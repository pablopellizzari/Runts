package com.example.runts.data.repository

import com.example.runts.data.remote.dto.ExternalActivityDto
import com.example.runts.data.remote.api.RuntsApiService
import com.example.runts.data.remote.dto.ApiErrorDto
import com.example.runts.data.security.EncryptedStorageManager
import com.example.runts.domain.model.StravaConnectionStatus
import com.example.runts.domain.model.StravaSyncResult
import com.example.runts.domain.repository.ExternalIntegrationRepository
import com.google.gson.Gson
import retrofit2.Response
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ExternalIntegrationRepositoryImpl @Inject constructor(
    private val api: RuntsApiService,
    private val storage: EncryptedStorageManager,
    private val gson: Gson
) : ExternalIntegrationRepository {
    private fun authorization(): String = storage.getAuthToken()?.let { "Bearer $it" }
        ?: throw IllegalStateException("Faça login novamente para acessar o Strava.")

    private fun <T> Response<T>.value(): T {
        if (isSuccessful) return body() ?: throw IllegalStateException("O servidor retornou uma resposta vazia.")
        val message = runCatching { gson.fromJson(errorBody()?.string(), ApiErrorDto::class.java).error }.getOrNull()
        throw IllegalStateException(message ?: "Não foi possível comunicar com o Runts (HTTP ${code()}).")
    }

    override suspend fun getStravaStatus() = resultOf {
        api.getStravaStatus(authorization()).value().let { StravaConnectionStatus(it.connected, it.athleteName, it.lastSyncAt) }
    }

    override suspend fun getStravaAuthorizationUrl() = resultOf { api.getStravaAuthorization(authorization()).value().authorizationUrl }

    override suspend fun syncStrava() = resultOf {
        api.syncStrava(authorization()).value().let { StravaSyncResult(it.imported, it.matched) }
    }

    override suspend fun disconnectStrava() = resultOf {
        val response = api.disconnectStrava(authorization())
        if (!response.isSuccessful) response.value()
    }

    override suspend fun getSuggestedStravaActivity(workoutId: String) = resultOf {
        val response = api.getStravaActivityForWorkout(authorization(), workoutId)
        if (!response.isSuccessful) response.value() else response.body()
    }
}
