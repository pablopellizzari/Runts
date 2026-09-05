package com.example.runts.domain.repository

import com.example.runts.domain.model.RaceEvent
import kotlinx.coroutines.flow.Flow

interface RaceEventRepository {
    suspend fun syncPending(): Result<Unit>
    fun getRaceEventsByAthlete(athleteId: String): Flow<List<RaceEvent>>
    suspend fun getRaceById(raceId: String): RaceEvent?
    suspend fun saveRaceEvent(raceEvent: RaceEvent): Result<Unit>
    suspend fun deleteRaceEvent(raceId: String, athleteId: String): Result<Unit>
    suspend fun syncRaceEventsWithRemote(athleteId: String): Result<Unit>
}
