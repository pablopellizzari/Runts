package com.example.runts.domain.repository

import com.example.runts.data.remote.dto.ExternalActivityDto

interface ExternalIntegrationRepository {
    suspend fun fetchStravaActivities(token: String): Result<List<ExternalActivityDto>>
    suspend fun fetchGarminActivities(athleteId: String, token: String): Result<List<ExternalActivityDto>>
}
