package com.example.runts.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.runts.domain.model.RaceEvent
import com.example.runts.domain.model.RacePriority

/**
 * Entidade Room para persistência de Provas do Calendário.
 */
@Entity(tableName = "race_events")
data class RaceEventEntity(
    @PrimaryKey val id: String,
    val athleteId: String,
    val name: String,
    val date: String,
    val modality: String,
    val targetTime: String? = null,
    val priority: RacePriority,
    val pendingSync: Boolean = false
)

fun RaceEventEntity.toDomain() = RaceEvent(
    id = id,
    athleteId = athleteId,
    name = name,
    date = date,
    modality = modality,
    targetTime = targetTime,
    priority = priority,
    pendingSync = pendingSync
)

fun RaceEvent.toEntity() = RaceEventEntity(
    id = id,
    athleteId = athleteId,
    name = name,
    date = date,
    modality = modality,
    targetTime = targetTime,
    priority = priority,
    pendingSync = pendingSync
)
