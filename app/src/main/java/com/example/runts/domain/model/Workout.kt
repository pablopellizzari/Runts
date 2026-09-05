package com.example.runts.domain.model

/**
 * Modelo de domínio para Treino Prescrito na Planilha.
 */
data class Workout(
    val id: String,
    val athleteId: String,
    val targetDate: String,              // Data formato YYYY-MM-DD
    val workoutType: WorkoutType,
    val targetDistanceKm: Double,
    val targetDurationMinutes: Int,
    val targetPace: String,              // ex: "5:30 min/km"
    val targetHeartRateZone: String,     // ex: "Z2 (135-145)"
    val description: String,
    val status: WorkoutStatus = WorkoutStatus.PENDING,
    val pendingSync: Boolean = false
)
