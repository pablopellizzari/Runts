package com.example.runts.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.runts.data.local.entity.SheetWorkoutEntity
import com.example.runts.data.local.entity.TrainingSheetEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TrainingSheetDao {
    @Query("SELECT * FROM training_sheets WHERE id = :id")
    suspend fun getById(id: String): TrainingSheetEntity?
    @Query("SELECT * FROM training_sheets WHERE pendingSync = 1")
    suspend fun getUnsynced(): List<TrainingSheetEntity>
    @Query("UPDATE training_sheets SET pendingSync = 0 WHERE id = :id")
    suspend fun markSynced(id: String)
    @Query("DELETE FROM sheet_workouts WHERE sheetId = :id")
    suspend fun deleteWorkouts(id: String)

    @Query("SELECT * FROM training_sheets WHERE coachId = :coachId ORDER BY createdAt DESC")
    fun getSheetsByCoach(coachId: String): Flow<List<TrainingSheetEntity>>

    @Query("SELECT * FROM sheet_workouts WHERE sheetId = :sheetId ORDER BY dayOfWeek ASC")
    fun getWorkoutsForSheet(sheetId: String): Flow<List<SheetWorkoutEntity>>

    @Query("SELECT * FROM sheet_workouts WHERE sheetId = :sheetId ORDER BY dayOfWeek ASC")
    suspend fun getWorkoutsForSheetOnce(sheetId: String): List<SheetWorkoutEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSheet(sheet: TrainingSheetEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSheetWorkouts(workouts: List<SheetWorkoutEntity>): List<Long>
}
