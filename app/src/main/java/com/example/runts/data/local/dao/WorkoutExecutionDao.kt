package com.example.runts.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.runts.data.local.entity.WorkoutExecutionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface WorkoutExecutionDao {
    @Query("SELECT * FROM workout_executions WHERE id = :id")
    suspend fun getById(id: String): WorkoutExecutionEntity?

    @Query("SELECT * FROM workout_executions WHERE athleteId = :athleteId ORDER BY executionDate DESC")
    fun getExecutionsByAthlete(athleteId: String): Flow<List<WorkoutExecutionEntity>>

    @Query("SELECT * FROM workout_executions WHERE prescribedWorkoutId = :workoutId LIMIT 1")
    suspend fun getExecutionForWorkout(workoutId: String): WorkoutExecutionEntity?

    @Query("SELECT * FROM workout_executions WHERE pendingSync = 1")
    suspend fun getUnsyncedExecutions(): List<WorkoutExecutionEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExecution(execution: WorkoutExecutionEntity): Long

    @Query("UPDATE workout_executions SET pendingSync = 0 WHERE id = :executionId")
    suspend fun markSynced(executionId: String): Int
}
