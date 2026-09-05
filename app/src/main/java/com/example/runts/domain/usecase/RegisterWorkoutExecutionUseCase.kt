package com.example.runts.domain.usecase

import com.example.runts.data.repository.resultOf
import com.example.runts.data.security.EncryptedStorageManager
import com.example.runts.domain.model.*
import com.example.runts.domain.repository.WorkoutRepository
import java.util.UUID
import javax.inject.Inject

class RegisterWorkoutExecutionUseCase @Inject constructor(
    private val workoutRepository: WorkoutRepository,
    private val encryptedStorageManager: EncryptedStorageManager
) {
    suspend operator fun invoke(
        prescribedWorkoutId: String?, athleteId: String, executionDate: String,
        actualDistanceKm: Double, actualDurationSeconds: Int, actualPace: String,
        actualAvgHeartRate: Int?, pse: Int, rawGpsDataJson: String?, comments: String?
    ): Result<Unit> = resultOf {
        val workout = prescribedWorkoutId?.let { workoutRepository.getWorkoutById(it) }
        val execution = WorkoutExecution(
            id = prescribedWorkoutId?.let { UUID.nameUUIDFromBytes("execution:$it".toByteArray()).toString() } ?: UUID.randomUUID().toString(),
            prescribedWorkoutId = prescribedWorkoutId, athleteId = athleteId,
            executionDate = TrainingRules.date(executionDate).toString(),
            actualDistanceKm = actualDistanceKm, actualDurationSeconds = actualDurationSeconds,
            actualPace = actualPace.trim().takeIf { it.isNotEmpty() }
                ?: TrainingRules.pace(actualDistanceKm, actualDurationSeconds),
            actualAvgHeartRate = actualAvgHeartRate, pse = pse,
            encryptedGpsDataJson = rawGpsDataJson?.let { encryptedStorageManager.encryptSensitiveHealthData(it) },
            comments = comments?.trim()?.takeIf { it.isNotEmpty() }, pendingSync = true
        )
        TrainingRules.execution(execution, workout)
        require(workout == null || workout.status == WorkoutStatus.PENDING) { "Este treino já foi concluído ou cancelado." }
        workoutRepository.saveWorkoutExecution(execution).getOrThrow()
    }
}
