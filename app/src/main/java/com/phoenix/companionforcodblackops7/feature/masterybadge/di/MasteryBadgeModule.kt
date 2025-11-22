package com.phoenix.companionforcodblackops7.feature.masterybadge.di

import com.phoenix.companionforcodblackops7.feature.masterybadge.data.repository.MasteryBadgeRepositoryImpl
import com.phoenix.companionforcodblackops7.feature.masterybadge.domain.repository.MasteryBadgeRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Hilt module for mastery badge feature dependencies
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class MasteryBadgeModule {

    @Binds
    @Singleton
    abstract fun bindMasteryBadgeRepository(
        impl: MasteryBadgeRepositoryImpl
    ): MasteryBadgeRepository
}
