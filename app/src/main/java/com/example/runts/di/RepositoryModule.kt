package com.example.runts.di

import com.example.runts.data.repository.ExternalIntegrationRepositoryImpl
import com.example.runts.data.repository.RaceEventRepositoryImpl
import com.example.runts.data.repository.TrainingSheetRepositoryImpl
import com.example.runts.data.repository.UserRepositoryImpl
import com.example.runts.data.repository.WorkoutRepositoryImpl
import com.example.runts.domain.repository.ExternalIntegrationRepository
import com.example.runts.domain.repository.RaceEventRepository
import com.example.runts.domain.repository.TrainingSheetRepository
import com.example.runts.domain.repository.UserRepository
import com.example.runts.domain.repository.WorkoutRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindUserRepository(
        impl: UserRepositoryImpl
    ): UserRepository

    @Binds
    @Singleton
    abstract fun bindWorkoutRepository(
        impl: WorkoutRepositoryImpl
    ): WorkoutRepository

    @Binds
    @Singleton
    abstract fun bindRaceEventRepository(
        impl: RaceEventRepositoryImpl
    ): RaceEventRepository

    @Binds
    @Singleton
    abstract fun bindExternalIntegrationRepository(
        impl: ExternalIntegrationRepositoryImpl
    ): ExternalIntegrationRepository

    @Binds
    @Singleton
    abstract fun bindTrainingSheetRepository(
        impl: TrainingSheetRepositoryImpl
    ): TrainingSheetRepository
}
