package com.example.runts.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.runts.data.local.entity.UserEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {

    @Query("UPDATE users SET coachId = NULL WHERE coachId = :coachId AND id NOT IN (:ids)")
    suspend fun unlinkMissing(coachId: String, ids: List<String>)

    @Query("SELECT * FROM users WHERE id = :userId")
    fun getUserById(userId: String): Flow<UserEntity?>

    @Query("SELECT * FROM users WHERE LOWER(email) = LOWER(:email) LIMIT 1")
    suspend fun getUserByEmail(email: String): UserEntity?

    @Query("SELECT * FROM users WHERE coachId = :coachId")
    fun getAthletesByCoachId(coachId: String): Flow<List<UserEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: UserEntity): Long

    @Query("UPDATE users SET coachId = :coachId WHERE id = :athleteId")
    suspend fun linkAthleteToCoach(athleteId: String, coachId: String): Int

    @Query("SELECT * FROM users WHERE inviteCode = :code LIMIT 1")
    suspend fun getUserByInviteCode(code: String): UserEntity?
}
