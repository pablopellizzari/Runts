package com.example.runts.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.runts.domain.model.User
import com.example.runts.domain.model.UserType

/**
 * Entidade Room para persistência de Usuários offline.
 */
@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey val id: String,
    val name: String,
    val email: String,
    val userType: UserType,
    val coachId: String? = null,
    val inviteCode: String? = null,
    val passwordHash: String? = null
)

fun UserEntity.toDomain() = User(
    id = id,
    name = name,
    email = email,
    userType = userType,
    coachId = coachId,
    inviteCode = inviteCode
)

fun User.toEntity() = UserEntity(
    id = id,
    name = name,
    email = email,
    userType = userType,
    coachId = coachId,
    inviteCode = inviteCode
)
