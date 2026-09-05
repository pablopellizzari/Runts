package com.example.runts.data.repository

import com.example.runts.data.local.dao.UserDao
import com.example.runts.data.local.entity.toDomain
import com.example.runts.data.local.entity.toEntity
import com.example.runts.data.remote.database.NeonPostgresManager
import com.example.runts.domain.model.User
import com.example.runts.domain.model.UserType
import com.example.runts.domain.repository.UserRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserRepositoryImpl @Inject constructor(
    private val userDao: UserDao,
    private val neonPostgresManager: NeonPostgresManager
) : UserRepository {

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
        return withContext(Dispatchers.IO) {
            val result = neonPostgresManager.loginUser(email, pass)
            result.fold(
                onSuccess = { user ->
                    if (user.userType != UserType.ATHLETE) {
                        Result.failure(IllegalArgumentException("A conta de treinador deve ser acessada pelo painel web."))
                    } else {
                        userDao.insertUser(user.toEntity())
                        Result.success(user)
                    }
                },
                onFailure = { Result.failure(it) }
            )
        }
    }

    override suspend fun registerUser(name: String, email: String, pass: String, userType: UserType): Result<User> {
        return withContext(Dispatchers.IO) {
            require(userType == UserType.ATHLETE) { "O aplicativo permite apenas cadastro de atletas." }
            val result = neonPostgresManager.registerUser(name, email, pass, UserType.ATHLETE)
            result.onSuccess { user ->
                userDao.insertUser(user.toEntity())
            }
            result
        }
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
