package com.example.runts.data.remote.dto

import com.example.runts.domain.model.RaceEvent
import com.example.runts.domain.model.RacePriority
import com.google.gson.annotations.SerializedName

data class RaceEventDto(
    @SerializedName("id") val id: String,
    @SerializedName("athlete_id") val athleteId: String,
    @SerializedName("name") val name: String,
    @SerializedName("date") val date: String,
    @SerializedName("modality") val modality: String,
    @SerializedName("target_time") val targetTime: String? = null,
    @SerializedName("priority") val priority: String
)

fun RaceEventDto.toDomain() = RaceEvent(
    id = id,
    athleteId = athleteId,
    name = name,
    date = date,
    modality = modality,
    targetTime = targetTime,
    priority = RacePriority.valueOf(priority),
    pendingSync = false
)
