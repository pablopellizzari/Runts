package com.example.runts.domain.model

/**
 * Modelo de domínio para Planilha Semanal de Treinos criada pelo Treinador.
 */
data class TrainingSheet(
    val id: String,
    val coachId: String,
    val title: String,                // Ex: "Planilha Maratona - Mês 1"
    val description: String? = null,
    val createdAt: String,
    val workouts: List<SheetWorkout> = emptyList()
)
