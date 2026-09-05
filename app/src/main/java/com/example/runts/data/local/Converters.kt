package com.example.runts.data.local

import androidx.room.TypeConverter
import com.example.runts.domain.model.RacePriority
import com.example.runts.domain.model.UserType
import com.example.runts.domain.model.WorkoutStatus
import com.example.runts.domain.model.WorkoutType

/**
 * Conversores de tipo para suporte de Enums no Room Database.
 */
class Converters {

    @TypeConverter
    fun fromUserType(value: UserType): String = value.name

    @TypeConverter
    fun toUserType(value: String): UserType = UserType.valueOf(value)

    @TypeConverter
    fun fromWorkoutType(value: WorkoutType): String = value.name

    @TypeConverter
    fun toWorkoutType(value: String): WorkoutType = WorkoutType.valueOf(value)

    @TypeConverter
    fun fromWorkoutStatus(value: WorkoutStatus): String = value.name

    @TypeConverter
    fun toWorkoutStatus(value: String): WorkoutStatus = WorkoutStatus.valueOf(value)

    @TypeConverter
    fun fromRacePriority(value: RacePriority): String = value.name

    @TypeConverter
    fun toRacePriority(value: String): RacePriority = RacePriority.valueOf(value)
}
