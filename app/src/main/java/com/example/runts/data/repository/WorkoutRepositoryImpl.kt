package com.example.runts.data.repository

import androidx.room.withTransaction
import com.example.runts.data.local.RuntsDatabase
import com.example.runts.data.local.entity.*
import com.example.runts.data.remote.database.NeonPostgresManager
import com.example.runts.data.worker.SyncWorkManager
import com.example.runts.domain.model.*
import com.example.runts.domain.repository.WorkoutRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WorkoutRepositoryImpl @Inject constructor(
    private val db: RuntsDatabase,
    private val remote: NeonPostgresManager,
    private val scheduler: SyncWorkManager
) : WorkoutRepository {
    private val workouts get() = db.workoutDao()
    private val executions get() = db.workoutExecutionDao()
    private val syncLock = Mutex()

    override fun getWorkoutsByAthlete(athleteId: String): Flow<List<Workout>> = offlineFlow(
        workouts.getWorkoutsByAthlete(athleteId).map { list -> list.map { it.toDomain() } }
    ) { syncWorkoutsWithRemote(athleteId).getOrThrow() }

    override fun getTodayWorkout(athleteId: String, date: String) =
        getWorkoutsByAthlete(athleteId).map { it.firstOrNull { w -> w.targetDate == date } }

    override suspend fun getWorkoutById(workoutId: String) = workouts.getWorkoutById(workoutId)?.toDomain()
    override suspend fun getExecutionForWorkout(workoutId: String) = executions.getExecutionForWorkout(workoutId)?.toDomain()

    override suspend fun syncWorkoutsWithRemote(athleteId: String): Result<Unit> = resultOf {
        val fresh = remote.getWorkoutsByAthlete(athleteId)
        db.withTransaction {
            val pendingExecutions = executions.getUnsyncedExecutions().mapNotNull { it.prescribedWorkoutId }.toSet()
            fresh.forEach { w ->
                if (workouts.getWorkoutById(w.id)?.pendingSync != true) {
                    workouts.insertWorkout(w.copy(status = if (w.id in pendingExecutions) WorkoutStatus.COMPLETED else w.status).toEntity())
                }
            }
        }
    }
    override suspend fun saveWorkoutPrescription(workout: Workout): Result<Unit> = resultOf {
        TrainingRules.prescription(workout)
        val existing = workouts.getWorkoutById(workout.id)
        require(existing == null || (existing.status == WorkoutStatus.PENDING && !TrainingRules.date(workout.targetDate).isBefore(java.time.LocalDate.now()))) {
            "Somente treinos futuros em aberto podem ser editados."
        }
        workouts.insertWorkout(workout.copy(pendingSync = true).toEntity())
        scheduler.syncImmediately()
    }
    override suspend fun saveWorkoutExecution(execution: WorkoutExecution): Result<Unit> = resultOf {
        db.withTransaction {
            val w = execution.prescribedWorkoutId?.let { workouts.getWorkoutById(it)?.toDomain() }
            TrainingRules.execution(execution, w)
            require(w == null || w.status == WorkoutStatus.PENDING) { "Treino já concluído." }
            executions.insertExecution(execution.copy(pendingSync = true).toEntity())
            execution.prescribedWorkoutId?.let { workouts.updateWorkoutStatus(it,WorkoutStatus.COMPLETED.name,w?.pendingSync ?: false) }
        }
        remote.saveWorkoutExecution(execution).fold(
            onSuccess = {
                db.withTransaction {
                    if (executions.getById(execution.id)?.pendingSync == true) executions.markSynced(execution.id)
                    execution.prescribedWorkoutId?.let { workouts.updateWorkoutStatus(it, WorkoutStatus.COMPLETED.name, false) }
                }
            },
            onFailure = { scheduler.syncImmediately() }
        )
    }
    override suspend fun getUnsyncedExecutions() = executions.getUnsyncedExecutions().map { it.toDomain() }

    // Queue writes are durable before network access. Acknowledgements never erase a newer local edit.
    override suspend fun syncPendingExecutions(): Result<Unit> = syncLock.withLock { resultOf {
        workouts.getUnsyncedWorkouts().forEach { snapshot ->
            remote.saveWorkout(snapshot.toDomain()).getOrThrow()
            db.withTransaction { if (workouts.getWorkoutById(snapshot.id) == snapshot) workouts.markSynced(snapshot.id) }
        }
        executions.getUnsyncedExecutions().forEach { snapshot ->
            remote.saveWorkoutExecution(snapshot.toDomain()).getOrThrow()
            db.withTransaction { if (executions.getById(snapshot.id) == snapshot) executions.markSynced(snapshot.id) }
        }
    } }
    override fun getExecutionsByAthlete(athleteId: String): Flow<List<WorkoutExecution>> = offlineFlow(
        executions.getExecutionsByAthlete(athleteId).map { list -> list.map { it.toDomain() } }
    ) {
        val fresh = remote.getExecutionsByAthlete(athleteId)
        db.withTransaction {
            fresh.forEach { if (executions.getById(it.id)?.pendingSync != true) executions.insertExecution(it.toEntity()) }
        }
    }
}
