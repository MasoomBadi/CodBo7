package com.phoenix.companionforcodblackops7.feature.perks.di

import com.phoenix.companionforcodblackops7.feature.perks.data.repository.PerksRepositoryImpl
import com.phoenix.companionforcodblackops7.feature.perks.domain.repository.PerksRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Hilt module for providing Perks-related dependencies
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class PerksModule {

    @Binds
    @Singleton
    abstract fun bindPerksRepository(
        perksRepositoryImpl: PerksRepositoryImpl
    ): PerksRepository
}
