package com.example.runts.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.runts.domain.model.TrainingSheet

@Entity(tableName = "training_sheets")
data class TrainingSheetEntity(
    @PrimaryKey val id: String,
    val coachId: String,
    val title: String,
    val description: String? = null,
    val createdAt: String,
    @androidx.room.ColumnInfo(defaultValue = "0") val pendingSync: Boolean = false
)

fun TrainingSheetEntity.toDomain(workouts: List<com.example.runts.domain.model.SheetWorkout> = emptyList()) = TrainingSheet(
    id = id,
    coachId = coachId,
    title = title,
    description = description,
    createdAt = createdAt,
    workouts = workouts
)

fun TrainingSheet.toEntity() = TrainingSheetEntity(
    id = id,
    coachId = coachId,
    title = title,
    description = description,
    createdAt = createdAt,
    pendingSync = false
)
