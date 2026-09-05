package com.example.runts.domain.model

/**
 * Modelo de domínio para um treino individual que compõe uma Planilha de Treinos.
 */
data class SheetWorkout(
    val id: String,
    val sheetId: String,
    val dayOfWeek: Int,               // 1 = Segunda, 2 = Terça, ..., 7 = Domingo
    val workoutType: WorkoutType,
    val targetDistanceKm: Double,
    val targetDurationMinutes: Int,
    val targetPace: String,
    val targetHeartRateZone: String,
    val description: String
)
