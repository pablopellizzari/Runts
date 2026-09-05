package com.example.runts.domain.model

/**
 * Modelo agregador de métricas e estatísticas do atleta.
 */
data class AthleteStats(
    val totalDistanceKm: Double,
    val totalDurationSeconds: Int,
    val avgPace: String,
    val adherenceRatePercent: Double,                 // Taxa de adesão (%) = Concluídos / Prescritos
    val hrZoneDistributionPercent: Map<String, Int>,  // ex: {"Z1-Z2": 40, "Z3": 45, "Z4-Z5": 15}
    val avgPse: Double                                // Média da PSE (RPE 1-10)
)
