package com.example.runts.data.remote.dto

import com.example.runts.domain.model.WorkoutExecution
import com.google.gson.annotations.SerializedName

data class WorkoutExecutionDto(
    @SerializedName("id") val id: String,
    @SerializedName("prescribed_workout_id") val prescribedWorkoutId: String? = null,
    @SerializedName("athlete_id") val athleteId: String,
    @SerializedName("execution_date") val executionDate: String,
    @SerializedName("actual_distance_km") val actualDistanceKm: Double,
    @SerializedName("actual_duration_seconds") val actualDurationSeconds: Int,
    @SerializedName("actual_pace") val actualPace: String,
    @SerializedName("actual_avg_hr") val actualAvgHeartRate: Int? = null,
    @SerializedName("pse") val pse: Int,
    @SerializedName("encrypted_gps_data_json") val encryptedGpsDataJson: String? = null,
    @SerializedName("comments") val comments: String? = null,
    @SerializedName("source_provider") val sourceProvider: String? = null,
    @SerializedName("source_activity_id") val sourceActivityId: String? = null
)

fun WorkoutExecutionDto.toDomain() = WorkoutExecution(
    id = id,
    prescribedWorkoutId = prescribedWorkoutId,
    athleteId = athleteId,
    executionDate = executionDate,
    actualDistanceKm = actualDistanceKm,
    actualDurationSeconds = actualDurationSeconds,
    actualPace = actualPace,
    actualAvgHeartRate = actualAvgHeartRate,
    pse = pse,
    encryptedGpsDataJson = encryptedGpsDataJson,
    comments = comments,
    sourceProvider = sourceProvider,
    sourceActivityId = sourceActivityId,
    pendingSync = false
)
