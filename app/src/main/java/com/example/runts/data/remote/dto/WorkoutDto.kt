package com.example.runts.data.remote.dto

import com.example.runts.domain.model.Workout
import com.example.runts.domain.model.WorkoutStatus
import com.example.runts.domain.model.WorkoutType
import com.google.gson.annotations.SerializedName

data class WorkoutDto(
    @SerializedName("id") val id: String,
    @SerializedName("athlete_id") val athleteId: String,
    @SerializedName("target_date") val targetDate: String,
    @SerializedName("workout_type") val workoutType: String,
    @SerializedName("target_distance_km") val targetDistanceKm: Double,
    @SerializedName("target_duration_minutes") val targetDurationMinutes: Int,
    @SerializedName("target_pace") val targetPace: String,
    @SerializedName("target_hr_zone") val targetHeartRateZone: String,
    @SerializedName("description") val description: String,
    @SerializedName("status") val status: String
)

fun WorkoutDto.toDomain() = Workout(
    id = id,
    athleteId = athleteId,
    targetDate = targetDate,
    workoutType = WorkoutType.valueOf(workoutType),
    targetDistanceKm = targetDistanceKm,
    targetDurationMinutes = targetDurationMinutes,
    targetPace = targetPace,
    targetHeartRateZone = targetHeartRateZone,
    description = description,
    status = WorkoutStatus.valueOf(status),
    pendingSync = false
)
