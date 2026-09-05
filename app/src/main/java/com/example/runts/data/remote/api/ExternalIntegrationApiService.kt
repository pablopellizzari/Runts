package com.example.runts.data.remote.api

import com.example.runts.data.remote.dto.ExternalActivityDto
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Path

/**
 * Interface Retrofit (Mock) para sincronização com Strava / Garmin Connect.
 */
interface ExternalIntegrationApiService {

    @GET("strava/v3/athlete/activities")
    suspend fun getStravaActivities(
        @Header("Authorization") accessToken: String
    ): Response<List<ExternalActivityDto>>

    @GET("garmin/activities/{athleteId}")
    suspend fun getGarminActivities(
        @Path("athleteId") athleteId: String,
        @Header("Authorization") token: String
    ): Response<List<ExternalActivityDto>>
}
