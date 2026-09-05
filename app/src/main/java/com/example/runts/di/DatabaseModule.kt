package com.example.runts.di

import android.content.Context
import androidx.room.Room
import com.example.runts.data.local.RuntsDatabase
import com.example.runts.data.local.dao.RaceEventDao
import com.example.runts.data.local.dao.TrainingSheetDao
import com.example.runts.data.local.dao.UserDao
import com.example.runts.data.local.dao.WorkoutDao
import com.example.runts.data.local.dao.WorkoutExecutionDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideRuntsDatabase(
        @ApplicationContext context: Context,
    ): RuntsDatabase {
        return Room.databaseBuilder(
            context,
            RuntsDatabase::class.java,
            "runts_database.db"
        ).addMigrations(com.example.runts.data.local.DatabaseMigrations.ALL[0]).build()
    }

    @Provides
    fun provideUserDao(db: RuntsDatabase): UserDao = db.userDao()

    @Provides
    fun provideWorkoutDao(db: RuntsDatabase): WorkoutDao = db.workoutDao()

    @Provides
    fun provideRaceEventDao(db: RuntsDatabase): RaceEventDao = db.raceEventDao()

    @Provides
    fun provideWorkoutExecutionDao(db: RuntsDatabase): WorkoutExecutionDao = db.workoutExecutionDao()

    @Provides
    fun provideTrainingSheetDao(db: RuntsDatabase): TrainingSheetDao = db.trainingSheetDao()
}
