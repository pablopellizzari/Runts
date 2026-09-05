package com.example.runts.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.runts.data.local.entity.RaceEventEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface RaceEventDao {
    @Query("SELECT * FROM race_events WHERE id = :id")
    suspend fun getById(id: String): RaceEventEntity?
    @Query("UPDATE race_events SET pendingSync = 0 WHERE id = :id")
    suspend fun markSynced(id: String)

    @Query("SELECT * FROM race_events WHERE athleteId = :athleteId ORDER BY date ASC")
    fun getRaceEventsByAthlete(athleteId: String): Flow<List<RaceEventEntity>>

    @Query("SELECT * FROM race_events WHERE pendingSync = 1")
    suspend fun getUnsyncedRaceEvents(): List<RaceEventEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRaceEvent(raceEvent: RaceEventEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRaceEvents(raceEvents: List<RaceEventEntity>): List<Long>

    @Query("DELETE FROM race_events WHERE id = :id AND athleteId = :athleteId")
    suspend fun deleteById(id: String, athleteId: String): Int
}
