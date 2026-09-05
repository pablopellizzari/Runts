package com.example.runts.domain.repository

import com.example.runts.domain.model.Workout
import com.example.runts.domain.model.WorkoutExecution
import kotlinx.coroutines.flow.Flow

interface WorkoutRepository {
    fun getWorkoutsByAthlete(athleteId: String): Flow<List<Workout>>
    fun getTodayWorkout(athleteId: String, date: String): Flow<Workout?>
    suspend fun getWorkoutById(workoutId: String): Workout?
    suspend fun getExecutionForWorkout(workoutId: String): WorkoutExecution?
    suspend fun syncWorkoutsWithRemote(athleteId: String): Result<Unit>
    suspend fun saveWorkoutPrescription(workout: Workout): Result<Unit>
    suspend fun saveWorkoutExecution(execution: WorkoutExecution): Result<Unit>
    suspend fun getUnsyncedExecutions(): List<WorkoutExecution>
    suspend fun syncPendingExecutions(): Result<Unit>
    fun getExecutionsByAthlete(athleteId: String): Flow<List<WorkoutExecution>>
}
