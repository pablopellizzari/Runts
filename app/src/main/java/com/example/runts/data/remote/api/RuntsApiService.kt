package com.example.runts.data.remote.api

import com.example.runts.data.remote.dto.RaceEventDto
import com.example.runts.data.remote.dto.UserDto
import com.example.runts.data.remote.dto.WorkoutDto
import com.example.runts.data.remote.dto.WorkoutExecutionDto
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

/**
 * Retrofit API Service para comunicação com o backend do Runts.
 */
interface RuntsApiService {

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
