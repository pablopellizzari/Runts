package com.example.runts.domain.model

/**
 * Modelo de domínio para Treino Executado/Registrado pelo atleta.
 */
data class WorkoutExecution(
    val id: String,
    val prescribedWorkoutId: String? = null, // ID do treino prescrito associado (se houver)
    val athleteId: String,
    val executionDate: String,               // Data da corrida realizada
    val actualDistanceKm: Double,
    val actualDurationSeconds: Int,
    val actualPace: String,                  // Pace médio real (min/km)
    val actualAvgHeartRate: Int? = null,      // FC média real (BPM)
    val pse: Int,                            // Percepção Subjetiva de Esforço (1-10)
    val encryptedGpsDataJson: String? = null, // Dados sensíveis de geolocalização e relógio
    val comments: String? = null,             // Feedback do atleta
    val pendingSync: Boolean = false
)
