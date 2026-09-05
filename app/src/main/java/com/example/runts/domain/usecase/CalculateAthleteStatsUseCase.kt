package com.example.runts.domain.usecase

import com.example.runts.domain.model.AthleteStats
import com.example.runts.domain.model.WorkoutStatus
import com.example.runts.domain.repository.WorkoutRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import java.util.Locale
import javax.inject.Inject

/**
 * Caso de Uso (RF05): Cálculo de estatísticas agregadas do atleta:
 * - Distância total acumulada
 * - Tempo total
 * - Pace médio
 * - Taxa de adesão (%) = (Treinos Concluídos / Total Prescritos)
 * - Média de PSE (RPE)
 * - Distribuição em Zonas de Frequência Cardíaca
 */
class CalculateAthleteStatsUseCase @Inject constructor(
    private val workoutRepository: WorkoutRepository
) {
    operator fun invoke(athleteId: String): Flow<AthleteStats> {
        val workoutsFlow = workoutRepository.getWorkoutsByAthlete(athleteId)
        val executionsFlow = workoutRepository.getExecutionsByAthlete(athleteId)

        return combine(workoutsFlow, executionsFlow) { workouts, executions ->
            val totalPrescribed = workouts.size
            val completedWorkouts = workouts.count { it.status == WorkoutStatus.COMPLETED }
            val adherenceRate = if (totalPrescribed > 0) {
                (completedWorkouts.toDouble() / totalPrescribed.toDouble()) * 100.0
            } else {
                100.0
            }

            val totalDistance = executions.sumOf { it.actualDistanceKm }
            val totalDurationSeconds = executions.sumOf { it.actualDurationSeconds }

            val avgPse = if (executions.isNotEmpty()) {
                executions.map { it.pse }.average()
            } else {
                0.0
            }

            val avgPace = if (totalDistance > 0 && totalDurationSeconds > 0) {
                val totalPaceSecPerKm = totalDurationSeconds / totalDistance
                val minutes = (totalPaceSecPerKm / 60).toInt()
                val seconds = (totalPaceSecPerKm % 60).toInt()
                String.format(Locale.getDefault(), "%d:%02d /km", minutes, seconds)
            } else {
                "0:00 /km"
            }

            val withHr = executions.filter { it.actualAvgHeartRate != null }
            val hrZoneDistribution = if (withHr.isEmpty()) emptyMap() else {
                withHr.groupingBy { hrZone(it.actualAvgHeartRate!!) }.eachCount()
                    .mapValues { ((it.value * 100.0) / withHr.size).toInt() }
            }

            AthleteStats(
                totalDistanceKm = (totalDistance * 10).toInt() / 10.0,
                totalDurationSeconds = totalDurationSeconds,
                avgPace = avgPace,
                adherenceRatePercent = (adherenceRate * 10).toInt() / 10.0,
                hrZoneDistributionPercent = hrZoneDistribution,
                avgPse = (avgPse * 10).toInt() / 10.0
            )
        }
    }

    private fun hrZone(hr: Int) = when {
        hr < 120 -> "Z1"
        hr < 140 -> "Z2"
        hr < 160 -> "Z3"
        hr < 180 -> "Z4"
        else -> "Z5"
    }
}
