package com.example.runts.data.remote.dto

import com.google.gson.annotations.SerializedName

/**
 * DTO para dados sincronizados do Strava / Garmin Connect.
 */
data class ExternalActivityDto(
    @SerializedName("external_id") val externalId: String,
    @SerializedName("provider") val provider: String, // "STRAVA", "GARMIN"
    @SerializedName("name") val name: String,
    @SerializedName("distance_meters") val distanceMeters: Double,
    @SerializedName("moving_time_seconds") val movingTimeSeconds: Int,
    @SerializedName("average_speed_mps") val averageSpeedMps: Double,
    @SerializedName("average_heartrate") val averageHeartRate: Int?,
    @SerializedName("start_date_local") val startDateLocal: String
)
