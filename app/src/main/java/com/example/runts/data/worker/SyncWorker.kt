package com.example.runts.data.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.runts.domain.repository.WorkoutRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

/**
 * Worker do Android WorkManager (RNF03) para sincronização offline em segundo plano.
 * Sincroniza treinos realizados marcados com pendingSync = true quando a conexão é restabelecida.
 */
@HiltWorker
class SyncWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val workoutRepository: WorkoutRepository,
    private val raceRepository: com.example.runts.domain.repository.RaceEventRepository,
    private val sheetRepository: com.example.runts.domain.repository.TrainingSheetRepository
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        return try {
            sheetRepository.syncPending().getOrThrow()
            raceRepository.syncPending().getOrThrow()
            workoutRepository.syncPendingExecutions().fold(
                onSuccess = { Result.success() },
                onFailure = { Result.retry() }
            )
        } catch (e: kotlinx.coroutines.CancellationException) {
            throw e
        } catch (e: Exception) {
            Result.retry()
        }
    }
}
