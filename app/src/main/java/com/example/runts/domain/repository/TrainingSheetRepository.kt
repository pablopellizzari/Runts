package com.example.runts.domain.repository

import com.example.runts.domain.model.TrainingSheet
import kotlinx.coroutines.flow.Flow

interface TrainingSheetRepository {
    suspend fun syncPending(): Result<Unit>
    fun getSheetsByCoach(coachId: String): Flow<List<TrainingSheet>>
    suspend fun saveTrainingSheet(sheet: TrainingSheet): Result<Unit>
    suspend fun applySheetToAthlete(sheet: TrainingSheet, athleteId: String, startDate: java.time.LocalDate = java.time.LocalDate.now(), weeks: Int = 1): Result<Unit>
    suspend fun cloneAthletePreviousMonthWorkouts(athleteId: String): Result<Unit>
}
