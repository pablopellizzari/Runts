package com.example.runts.domain.repository

import com.example.runts.domain.model.User
import com.example.runts.domain.model.UserType
import kotlinx.coroutines.flow.Flow

interface UserRepository {
    fun getUserById(userId: String): Flow<User?>
    fun getAthletesByCoachId(coachId: String): Flow<List<User>>
    suspend fun loginUser(email: String, pass: String): Result<User>
    suspend fun registerUser(name: String, email: String, pass: String, userType: UserType): Result<User>
    suspend fun sendTemporaryPasswordEmail(email: String): Result<String>
    suspend fun linkAthleteToCoach(athleteId: String, inviteCode: String): Result<User>
    suspend fun saveUserLocally(user: User)
}
