package com.example.runts.data.repository

import androidx.room.withTransaction
import com.example.runts.data.local.RuntsDatabase
import com.example.runts.data.local.entity.*
import com.example.runts.data.remote.database.NeonPostgresManager
import com.example.runts.data.worker.SyncWorkManager
import com.example.runts.domain.model.*
import com.example.runts.domain.repository.TrainingSheetRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.time.LocalDate
import java.time.YearMonth
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TrainingSheetRepositoryImpl @Inject constructor(private val db: RuntsDatabase, private val remote: NeonPostgresManager, private val scheduler: SyncWorkManager) : TrainingSheetRepository {
    private val dao get() = db.trainingSheetDao()
    private val lock = Mutex()
    private suspend fun cache(s: TrainingSheet, pending: Boolean) {
        dao.insertSheet(s.toEntity().copy(pendingSync = pending))
        dao.deleteWorkouts(s.id)
        dao.insertSheetWorkouts(s.workouts.map { it.toEntity() })
    }
    override fun getSheetsByCoach(coachId: String): Flow<List<TrainingSheet>> = offlineFlow(
        dao.getSheetsByCoach(coachId).map { list -> list.map { it.toDomain(dao.getWorkoutsForSheetOnce(it.id).map { w -> w.toDomain() }) } }
    ) {
        val fresh = remote.getSheetsByCoach(coachId)
        db.withTransaction { fresh.forEach { if (dao.getById(it.id)?.pendingSync != true) cache(it,false) } }
    }
    override suspend fun saveTrainingSheet(sheet: TrainingSheet): Result<Unit> = resultOf {
        TrainingRules.sheet(sheet)
        db.withTransaction { cache(sheet,true) }
        scheduler.syncImmediately()
    }
    override suspend fun syncPending(): Result<Unit> = lock.withLock { resultOf {
        dao.getUnsynced().forEach { snapshot ->
            val children = dao.getWorkoutsForSheetOnce(snapshot.id)
            remote.saveTrainingSheet(snapshot.toDomain(children.map { it.toDomain() })).getOrThrow()
            db.withTransaction {
                if (dao.getById(snapshot.id) == snapshot && dao.getWorkoutsForSheetOnce(snapshot.id) == children) dao.markSynced(snapshot.id)
            }
        }
    } }
    override suspend fun applySheetToAthlete(sheet: TrainingSheet, athleteId: String, startDate: LocalDate, weeks: Int): Result<Unit> = resultOf {
        TrainingRules.sheet(sheet)
        require(weeks in 1..5) { "Escolha de 1 a 5 semanas." }
        require(!startDate.isBefore(LocalDate.now())) { "O início não pode estar no passado." }
        val monday = TrainingRules.weekStart(startDate)
        val prescriptions = (0 until weeks).flatMap { offset -> sheet.workouts.mapNotNull { sw ->
            val date = monday.plusWeeks(offset.toLong()).plusDays(sw.dayOfWeek - 1L)
            if (date.isBefore(startDate)) null else Workout(
                // Reapplying the same model/week updates open prescriptions, without duplicating days.
                UUID.nameUUIDFromBytes("${sheet.id}:$athleteId:${sw.id}:$date".toByteArray()).toString(),
                athleteId,date.toString(),sw.workoutType,sw.targetDistanceKm,sw.targetDurationMinutes,
                sw.targetPace,sw.targetHeartRateZone,sw.description,WorkoutStatus.PENDING,true)
        } }
        require(prescriptions.isNotEmpty()) { "Não há treinos nos dias escolhidos. Selecione outra semana." }
        db.withTransaction {
            prescriptions.forEach { w ->
                if (db.workoutDao().getWorkoutById(w.id)?.status != WorkoutStatus.COMPLETED) db.workoutDao().insertWorkout(w.toEntity())
            }
        }
        scheduler.syncImmediately()
    }
    override suspend fun cloneAthletePreviousMonthWorkouts(athleteId: String): Result<Unit> = resultOf {
        val current = YearMonth.now()
        val previous = current.minusMonths(1)
        val source = db.workoutDao().getWorkoutsByAthlete(athleteId).first().filter {
            runCatching { YearMonth.from(TrainingRules.date(it.targetDate)) == previous }.getOrDefault(false)
        }
        val copies = source.mapNotNull { old ->
            val day = TrainingRules.date(old.targetDate).dayOfMonth
            if (day > current.lengthOfMonth() || current.atDay(day).isBefore(LocalDate.now())) null
            else old.copy(id = UUID.nameUUIDFromBytes("clone:${old.id}:$current".toByteArray()).toString(),
                targetDate = current.atDay(day).toString(),status = WorkoutStatus.PENDING,pendingSync = true)
        }
        require(copies.isNotEmpty()) { "Sem treinos do mês anterior para os dias restantes deste mês." }
        db.withTransaction { copies.forEach { if (db.workoutDao().getWorkoutById(it.id) == null) db.workoutDao().insertWorkout(it) } }
        scheduler.syncImmediately()
    }
}
