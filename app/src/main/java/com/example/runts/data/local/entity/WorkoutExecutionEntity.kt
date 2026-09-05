package com.example.runts.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.runts.domain.model.WorkoutExecution

/**
 * Entidade Room para persistência de Treinos Executados pelo Atleta.
 */
@Entity(tableName = "workout_executions")
data class WorkoutExecutionEntity(
    @PrimaryKey val id: String,
    val prescribedWorkoutId: String? = null,
    val athleteId: String,
    val executionDate: String,
    val actualDistanceKm: Double,
    val actualDurationSeconds: Int,
    val actualPace: String,
    val actualAvgHeartRate: Int? = null,
    val pse: Int,
    val encryptedGpsDataJson: String? = null,
    val comments: String? = null,
    val sourceProvider: String? = null,
    val sourceActivityId: String? = null,
    val pendingSync: Boolean = false
)

fun WorkoutExecutionEntity.toDomain() = WorkoutExecution(
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
    pendingSync = pendingSync
)

fun WorkoutExecution.toEntity() = WorkoutExecutionEntity(
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
    pendingSync = pendingSync
)
