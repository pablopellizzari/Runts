package com.example.runts.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.runts.domain.model.Workout
import com.example.runts.domain.model.WorkoutStatus
import com.example.runts.domain.model.WorkoutType

/**
 * Entidade Room para persistência de Treinos Prescritos.
 */
@Entity(tableName = "workouts")
data class WorkoutEntity(
    @PrimaryKey val id: String,
    val athleteId: String,
    val targetDate: String,
    val workoutType: WorkoutType,
    val targetDistanceKm: Double,
    val targetDurationMinutes: Int,
    val targetPace: String,
    val targetHeartRateZone: String,
    val description: String,
    val status: WorkoutStatus,
    val pendingSync: Boolean = false
)

fun WorkoutEntity.toDomain() = Workout(
    id = id,
    athleteId = athleteId,
    targetDate = targetDate,
    workoutType = workoutType,
    targetDistanceKm = targetDistanceKm,
    targetDurationMinutes = targetDurationMinutes,
    targetPace = targetPace,
    targetHeartRateZone = targetHeartRateZone,
    description = description,
    status = status,
    pendingSync = pendingSync
)

fun Workout.toEntity() = WorkoutEntity(
    id = id,
    athleteId = athleteId,
    targetDate = targetDate,
    workoutType = workoutType,
    targetDistanceKm = targetDistanceKm,
    targetDurationMinutes = targetDurationMinutes,
    targetPace = targetPace,
    targetHeartRateZone = targetHeartRateZone,
    description = description,
    status = status,
    pendingSync = pendingSync
)
