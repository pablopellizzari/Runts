package com.example.runts.data.repository

import com.example.runts.data.remote.dto.ExternalActivityDto
import com.example.runts.domain.repository.ExternalIntegrationRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ExternalIntegrationRepositoryImpl @Inject constructor() : ExternalIntegrationRepository {

    override suspend fun fetchStravaActivities(token: String): Result<List<ExternalActivityDto>> {
        return Result.failure(UnsupportedOperationException("A integração Strava exige backend OAuth e ainda não foi configurada."))
    }

    override suspend fun fetchGarminActivities(athleteId: String, token: String): Result<List<ExternalActivityDto>> {
        return Result.failure(UnsupportedOperationException("A integração Garmin exige backend OAuth e ainda não foi configurada."))
    }
}
