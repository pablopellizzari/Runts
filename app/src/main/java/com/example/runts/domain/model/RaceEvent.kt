package com.example.runts.domain.model

/**
 * Modelo de domínio para Prova/Competição cadastrada no calendário.
 */
data class RaceEvent(
    val id: String,
    val athleteId: String,
    val name: String,
    val date: String,                    // Data formato YYYY-MM-DD
    val modality: String,                // 5K, 10K, 21K, 42K, etc.
    val targetTime: String? = null,      // ex: "01:45:00"
    val priority: RacePriority,          // PROVA_A ou PROVA_B
    val pendingSync: Boolean = false
)
