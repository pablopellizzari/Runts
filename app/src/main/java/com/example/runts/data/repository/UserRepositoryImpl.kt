package com.example.runts.data.repository

import com.example.runts.data.local.dao.UserDao
import com.example.runts.data.local.entity.toDomain
import com.example.runts.data.local.entity.toEntity
import com.example.runts.data.remote.database.NeonPostgresManager
import com.example.runts.data.remote.api.RuntsApiService
import com.example.runts.data.remote.dto.ApiErrorDto
import com.example.runts.data.remote.dto.AthleteAuthRequest
import com.example.runts.data.remote.dto.AthleteRegistrationRequest
import com.example.runts.data.remote.dto.toDomain
import com.example.runts.data.security.EncryptedStorageManager
import com.example.runts.domain.model.User
import com.example.runts.domain.model.UserType
import com.example.runts.domain.repository.UserRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import com.google.gson.Gson
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserRepositoryImpl @Inject constructor(
    private val userDao: UserDao,
    private val neonPostgresManager: NeonPostgresManager,
    private val api: RuntsApiService,
    private val encryptedStorageManager: EncryptedStorageManager,
    private val gson: Gson
) : UserRepository {

    private fun apiError(body: String?, status: Int): String = runCatching {
        gson.fromJson(body, ApiErrorDto::class.java).error
    }.getOrNull() ?: "Não foi possível comunicar com o Runts (HTTP $status)."

    override fun getUserById(userId: String): Flow<User?> {
        return offlineFlow(userDao.getUserById(userId).map { it?.toDomain() }) {
            neonPostgresManager.getUser(userId)?.let { userDao.insertUser(it.toEntity()) }
        }
    }

    override fun getAthletesByCoachId(coachId: String): Flow<List<User>> = flow {
        try {
            val remoteList = neonPostgresManager.getAthletesByCoachId(coachId)
            remoteList.forEach { userDao.insertUser(it.toEntity()) }
        } catch (t: Throwable) {
            t.printStackTrace()
        }

        userDao.getAthletesByCoachId(coachId).collect { list ->
            emit(list.map { it.toDomain() })
        }
    }

    override suspend fun loginUser(email: String, pass: String): Result<User> {
        return withContext(Dispatchers.IO) { resultOf {
            val response = api.loginAthlete(AthleteAuthRequest(email.trim(), pass))
            val authenticated = response.body().takeIf { response.isSuccessful }
                ?: error(apiError(response.errorBody()?.string(), response.code()))
            val user = authenticated.user.toDomain()
            require(user.userType == UserType.ATHLETE) { "A conta de treinador deve ser acessada pelo painel web." }
            encryptedStorageManager.saveAuthToken(authenticated.token)
            userDao.insertUser(user.toEntity())
            user
        } }
    }

    override suspend fun registerUser(name: String, email: String, pass: String, userType: UserType): Result<User> {
        return withContext(Dispatchers.IO) { resultOf {
            require(userType == UserType.ATHLETE) { "O aplicativo permite apenas cadastro de atletas." }
            val response = api.registerAthlete(AthleteRegistrationRequest(name.trim(), email.trim(), pass))
            val authenticated = response.body().takeIf { response.isSuccessful }
                ?: error(apiError(response.errorBody()?.string(), response.code()))
            val user = authenticated.user.toDomain()
            encryptedStorageManager.saveAuthToken(authenticated.token)
            userDao.insertUser(user.toEntity())
            user
        } }
    }

    override suspend fun sendTemporaryPasswordEmail(email: String): Result<String> {
        return withContext(Dispatchers.IO) {
            neonPostgresManager.sendTemporaryPasswordEmail(email)
        }
    }

    override suspend fun linkAthleteToCoach(athleteId: String, inviteCode: String): Result<User> {
        return withContext(Dispatchers.IO) {
            val result = neonPostgresManager.linkAthleteToCoach(athleteId, inviteCode)
            result.onSuccess { user ->
                userDao.insertUser(user.toEntity())
            }
            result
        }
    }

    override suspend fun saveUserLocally(user: User) {
        withContext(Dispatchers.IO) {
            userDao.insertUser(user.toEntity())
        }
    }
}
