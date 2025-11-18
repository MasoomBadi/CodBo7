package com.phoenix.companionforcodblackops7.feature.wildcards.di

import com.phoenix.companionforcodblackops7.feature.wildcards.data.repository.WildcardsRepositoryImpl
import com.phoenix.companionforcodblackops7.feature.wildcards.domain.repository.WildcardsRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Hilt module for providing Wildcards-related dependencies
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class WildcardsModule {

    @Binds
    @Singleton
    abstract fun bindWildcardsRepository(
        wildcardsRepositoryImpl: WildcardsRepositoryImpl
    ): WildcardsRepository
}
