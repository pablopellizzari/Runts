package com.example.runts.domain.usecase

import com.example.runts.domain.model.RaceEvent
import com.example.runts.domain.model.Workout
import com.example.runts.domain.repository.RaceEventRepository
import com.example.runts.domain.repository.WorkoutRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import javax.inject.Inject

/**
 * Sealed class para representar itens da timeline cronológica unificada.
 */
sealed class TimelineItem(val date: String) {
    data class WorkoutItem(val workout: Workout) : TimelineItem(workout.targetDate)
    data class RaceItem(val raceEvent: RaceEvent) : TimelineItem(raceEvent.date)
}

/**
 * Caso de Uso (RF03): Fusão de treinos prescritos (Workout) e eventos de prova (RaceEvent)
 * em uma única lista cronológica combinada para exibição no calendário do aluno.
 */
class GetCombinedTimelineUseCase @Inject constructor(
    private val workoutRepository: WorkoutRepository,
    private val raceEventRepository: RaceEventRepository
) {
    operator fun invoke(athleteId: String): Flow<List<TimelineItem>> {
        val workoutsFlow = workoutRepository.getWorkoutsByAthlete(athleteId)
        val racesFlow = raceEventRepository.getRaceEventsByAthlete(athleteId)

        return combine(workoutsFlow, racesFlow) { workouts, races ->
            val workoutItems: List<TimelineItem> = workouts.map { TimelineItem.WorkoutItem(it) }
            val raceItems: List<TimelineItem> = races.map { TimelineItem.RaceItem(it) }

            (workoutItems + raceItems).sortedBy { it.date }
        }
    }
}
