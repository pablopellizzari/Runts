package com.example.runts.data.remote.dto

import com.example.runts.domain.model.User
import com.example.runts.domain.model.UserType
import com.google.gson.annotations.SerializedName

data class UserDto(
    @SerializedName("id") val id: String,
    @SerializedName("name") val name: String,
    @SerializedName("email") val email: String,
    @SerializedName("user_type") val userType: String,
    @SerializedName("coach_id") val coachId: String? = null,
    @SerializedName("invite_code") val inviteCode: String? = null
)

fun UserDto.toDomain() = User(
    id = id,
    name = name,
    email = email,
    userType = UserType.valueOf(userType),
    coachId = coachId,
    inviteCode = inviteCode
)
