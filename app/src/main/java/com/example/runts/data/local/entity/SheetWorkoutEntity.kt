package com.example.runts.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.runts.domain.model.SheetWorkout
import com.example.runts.domain.model.WorkoutType

@Entity(tableName = "sheet_workouts")
data class SheetWorkoutEntity(
    @PrimaryKey val id: String,
    val sheetId: String,
    val dayOfWeek: Int,
    val workoutType: WorkoutType,
    val targetDistanceKm: Double,
    val targetDurationMinutes: Int,
    val targetPace: String,
    val targetHeartRateZone: String,
    val description: String
)

fun SheetWorkoutEntity.toDomain() = SheetWorkout(
    id = id,
    sheetId = sheetId,
    dayOfWeek = dayOfWeek,
    workoutType = workoutType,
    targetDistanceKm = targetDistanceKm,
    targetDurationMinutes = targetDurationMinutes,
    targetPace = targetPace,
    targetHeartRateZone = targetHeartRateZone,
    description = description
)

fun SheetWorkout.toEntity() = SheetWorkoutEntity(
    id = id,
    sheetId = sheetId,
    dayOfWeek = dayOfWeek,
    workoutType = workoutType,
    targetDistanceKm = targetDistanceKm,
    targetDurationMinutes = targetDurationMinutes,
    targetPace = targetPace,
    targetHeartRateZone = targetHeartRateZone,
    description = description
)
