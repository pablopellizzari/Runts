package com.example.runts.domain.usecase

import com.example.runts.domain.repository.RaceEventRepository
import com.example.runts.domain.repository.WorkoutRepository
import javax.inject.Inject

/**
 * Caso de Uso para sincronizar planilhas de treino e calendário de provas.
 * Tenta buscar da API remota e persiste no Room Database (Single Source of Truth / RNF03).
 */
class SyncWorkoutsUseCase @Inject constructor(
    private val workoutRepository: WorkoutRepository,
    private val raceEventRepository: RaceEventRepository
) {
    suspend operator fun invoke(athleteId: String): Result<Unit> {
        val workoutResult = workoutRepository.syncWorkoutsWithRemote(athleteId)
        val raceResult = raceEventRepository.syncRaceEventsWithRemote(athleteId)

        return if (workoutResult.isSuccess && raceResult.isSuccess) {
            Result.success(Unit)
        } else {
            // Em caso de offline/falha, o Room mantém a cópia local mais recente
            Result.failure(workoutResult.exceptionOrNull() ?: raceResult.exceptionOrNull() ?: Exception("Erro de sincronização"))
        }
    }
}
