package com.example.runts.domain.repository

import com.example.runts.data.remote.dto.ExternalActivityDto
import com.example.runts.domain.model.StravaConnectionStatus
import com.example.runts.domain.model.StravaSyncResult

interface ExternalIntegrationRepository {
    suspend fun getStravaStatus(): Result<StravaConnectionStatus>
    suspend fun getStravaAuthorizationUrl(): Result<String>
    suspend fun syncStrava(): Result<StravaSyncResult>
    suspend fun disconnectStrava(): Result<Unit>
    suspend fun getSuggestedStravaActivity(workoutId: String): Result<ExternalActivityDto?>
}
