package com.example.runts.data.remote.api

import com.example.runts.data.remote.dto.RaceEventDto
import com.example.runts.data.remote.dto.UserDto
import com.example.runts.data.remote.dto.WorkoutDto
import com.example.runts.data.remote.dto.WorkoutExecutionDto
import com.example.runts.data.remote.dto.AthleteAuthRequest
import com.example.runts.data.remote.dto.AthleteAuthResponse
import com.example.runts.data.remote.dto.AthleteRegistrationRequest
import com.example.runts.data.remote.dto.ExternalActivityDto
import com.example.runts.data.remote.dto.StravaAuthorizationDto
import com.example.runts.data.remote.dto.StravaStatusDto
import com.example.runts.data.remote.dto.StravaSyncResultDto
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

/**
 * Retrofit API Service para comunicação com o backend do Runts.
 */
interface RuntsApiService {

    @POST("athlete/auth/login")
    suspend fun loginAthlete(@Body request: AthleteAuthRequest): Response<AthleteAuthResponse>

    @POST("athlete/auth/register")
    suspend fun registerAthlete(@Body request: AthleteRegistrationRequest): Response<AthleteAuthResponse>

    @GET("athlete/integrations/strava/status")
    suspend fun getStravaStatus(@Header("Authorization") authorization: String): Response<StravaStatusDto>

    @GET("athlete/integrations/strava/authorize")
    suspend fun getStravaAuthorization(@Header("Authorization") authorization: String): Response<StravaAuthorizationDto>

    @POST("athlete/integrations/strava/sync")
    suspend fun syncStrava(@Header("Authorization") authorization: String): Response<StravaSyncResultDto>

    @DELETE("athlete/integrations/strava")
    suspend fun disconnectStrava(@Header("Authorization") authorization: String): Response<Unit>

    @GET("athlete/workouts/{workoutId}/strava-activity")
    suspend fun getStravaActivityForWorkout(
        @Header("Authorization") authorization: String,
        @Path("workoutId") workoutId: String
    ): Response<ExternalActivityDto?>

    @GET("users/{userId}")
    suspend fun getUserProfile(@Path("userId") userId: String): Response<UserDto>

    @POST("users/link")
    suspend fun linkAthleteToCoach(
        @Query("athleteId") athleteId: String,
        @Query("inviteCode") inviteCode: String
    ): Response<UserDto>

    @GET("workouts/athlete/{athleteId}")
    suspend fun getWorkouts(@Path("athleteId") athleteId: String): Response<List<WorkoutDto>>

    @POST("workouts")
    suspend fun syncWorkouts(@Body workouts: List<WorkoutDto>): Response<ResponseBody>

    @GET("races/athlete/{athleteId}")
    suspend fun getRaceEvents(@Path("athleteId") athleteId: String): Response<List<RaceEventDto>>

    @POST("races")
    suspend fun syncRaceEvents(@Body raceEvents: List<RaceEventDto>): Response<ResponseBody>

    @POST("executions")
    suspend fun syncWorkoutExecutions(@Body executions: List<WorkoutExecutionDto>): Response<ResponseBody>
}
