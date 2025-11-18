package com.phoenix.companionforcodblackops7.feature.tacticals.di

import com.phoenix.companionforcodblackops7.feature.tacticals.data.repository.TacticalsRepositoryImpl
import com.phoenix.companionforcodblackops7.feature.tacticals.domain.repository.TacticalsRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Hilt module for providing Tacticals-related dependencies
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class TacticalsModule {

    @Binds
    @Singleton
    abstract fun bindTacticalsRepository(
        tacticalsRepositoryImpl: TacticalsRepositoryImpl
    ): TacticalsRepository
}
