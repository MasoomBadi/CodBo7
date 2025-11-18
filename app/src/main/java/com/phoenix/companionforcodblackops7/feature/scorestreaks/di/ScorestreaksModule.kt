package com.phoenix.companionforcodblackops7.feature.scorestreaks.di

import com.phoenix.companionforcodblackops7.feature.scorestreaks.data.repository.ScorestreaksRepositoryImpl
import com.phoenix.companionforcodblackops7.feature.scorestreaks.domain.repository.ScorestreaksRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Hilt module for providing Scorestreaks-related dependencies
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class ScorestreaksModule {

    @Binds
    @Singleton
    abstract fun bindScorestreaksRepository(
        scorestreaksRepositoryImpl: ScorestreaksRepositoryImpl
    ): ScorestreaksRepository
}
