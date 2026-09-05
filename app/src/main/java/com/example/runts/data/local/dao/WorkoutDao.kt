package com.example.runts.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.runts.data.local.entity.WorkoutEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface WorkoutDao {
    @Query("UPDATE workouts SET pendingSync = 0 WHERE id = :id")
    suspend fun markSynced(id: String)

    @Query("SELECT * FROM workouts WHERE athleteId = :athleteId ORDER BY targetDate ASC")
    fun getWorkoutsByAthlete(athleteId: String): Flow<List<WorkoutEntity>>

    @Query("SELECT * FROM workouts WHERE athleteId = :athleteId AND targetDate = :date LIMIT 1")
    fun getTodayWorkout(athleteId: String, date: String): Flow<WorkoutEntity?>

    @Query("SELECT * FROM workouts WHERE id = :workoutId")
    suspend fun getWorkoutById(workoutId: String): WorkoutEntity?

    @Query("SELECT * FROM workouts WHERE pendingSync = 1")
    suspend fun getUnsyncedWorkouts(): List<WorkoutEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWorkout(workout: WorkoutEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWorkouts(workouts: List<WorkoutEntity>): List<Long>

    @Query("UPDATE workouts SET status = :status, pendingSync = :pendingSync WHERE id = :workoutId")
    suspend fun updateWorkoutStatus(workoutId: String, status: String, pendingSync: Boolean): Int
}
