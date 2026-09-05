package com.example.runts.data.repository

import androidx.room.withTransaction
import com.example.runts.data.local.RuntsDatabase
import com.example.runts.data.local.entity.*
import com.example.runts.data.remote.database.NeonPostgresManager
import com.example.runts.data.worker.SyncWorkManager
import com.example.runts.domain.model.*
import com.example.runts.domain.repository.RaceEventRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RaceEventRepositoryImpl @Inject constructor(private val db: RuntsDatabase, private val remote: NeonPostgresManager, private val scheduler: SyncWorkManager) : RaceEventRepository {
    private val dao get() = db.raceEventDao()
    private val lock = Mutex()
    override fun getRaceEventsByAthlete(athleteId: String): Flow<List<RaceEvent>> = offlineFlow(
        dao.getRaceEventsByAthlete(athleteId).map { list -> list.map { it.toDomain() } }
    ) { syncRaceEventsWithRemote(athleteId).getOrThrow() }
    override suspend fun getRaceById(raceId: String): RaceEvent? = dao.getById(raceId)?.toDomain()
    override suspend fun saveRaceEvent(raceEvent: RaceEvent): Result<Unit> = resultOf {
        TrainingRules.race(raceEvent)
        dao.insertRaceEvent(raceEvent.copy(pendingSync = true).toEntity())
        remote.saveRaceEvent(raceEvent).fold(
            onSuccess = {
                db.withTransaction {
                    if (dao.getById(raceEvent.id)?.pendingSync == true) dao.markSynced(raceEvent.id)
                }
            },
            onFailure = { scheduler.syncImmediately() }
        )
    }
    override suspend fun syncPending(): Result<Unit> = lock.withLock { resultOf {
        dao.getUnsyncedRaceEvents().forEach { snapshot ->
            remote.saveRaceEvent(snapshot.toDomain()).getOrThrow()
            db.withTransaction { if (dao.getById(snapshot.id) == snapshot) dao.markSynced(snapshot.id) }
        }
    } }
    override suspend fun deleteRaceEvent(raceId: String, athleteId: String): Result<Unit> = resultOf {
        remote.deleteRaceEvent(raceId, athleteId).getOrThrow()
        dao.deleteById(raceId, athleteId)
    }
    override suspend fun syncRaceEventsWithRemote(athleteId: String): Result<Unit> = resultOf {
        val fresh = remote.getRaceEventsByAthlete(athleteId)
        db.withTransaction { fresh.forEach { if (dao.getById(it.id)?.pendingSync != true) dao.insertRaceEvent(it.toEntity()) } }
    }
}
