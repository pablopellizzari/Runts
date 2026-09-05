package com.example.runts.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.runts.data.local.dao.RaceEventDao
import com.example.runts.data.local.dao.TrainingSheetDao
import com.example.runts.data.local.dao.UserDao
import com.example.runts.data.local.dao.WorkoutDao
import com.example.runts.data.local.dao.WorkoutExecutionDao
import com.example.runts.data.local.entity.RaceEventEntity
import com.example.runts.data.local.entity.SheetWorkoutEntity
import com.example.runts.data.local.entity.TrainingSheetEntity
import com.example.runts.data.local.entity.UserEntity
import com.example.runts.data.local.entity.WorkoutEntity
import com.example.runts.data.local.entity.WorkoutExecutionEntity

@Database(
    entities = [
        UserEntity::class,
        WorkoutEntity::class,
        RaceEventEntity::class,
        WorkoutExecutionEntity::class,
        TrainingSheetEntity::class,
        SheetWorkoutEntity::class
    ],
    version = 4,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class RuntsDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun workoutDao(): WorkoutDao
    abstract fun raceEventDao(): RaceEventDao
    abstract fun workoutExecutionDao(): WorkoutExecutionDao
    abstract fun trainingSheetDao(): TrainingSheetDao
}
