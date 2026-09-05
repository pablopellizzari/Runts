package com.example.runts.data.remote.dto

import com.google.gson.annotations.SerializedName

data class AthleteAuthRequest(
    val email: String,
    val password: String
)

data class AthleteRegistrationRequest(
    val name: String,
    val email: String,
    val password: String,
    val timezone: String = "America/Sao_Paulo"
)

data class AthleteAuthResponse(
    val token: String,
    val user: UserDto
)

data class StravaStatusDto(
    val connected: Boolean,
    val athleteName: String? = null,
    val scope: String? = null,
    val lastSyncAt: String? = null,
    val connectedAt: String? = null
)

data class StravaAuthorizationDto(
    val authorizationUrl: String
)

data class StravaSyncResultDto(
    val imported: Int,
    val matched: Int
)

data class ApiErrorDto(
    @SerializedName("error") val error: String? = null
)
