package com.phoenix.companionforcodblackops7.feature.lethals.di

import com.phoenix.companionforcodblackops7.feature.lethals.data.repository.LethalsRepositoryImpl
import com.phoenix.companionforcodblackops7.feature.lethals.domain.repository.LethalsRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Hilt module for Lethals feature dependencies
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class LethalsModule {

    @Binds
    @Singleton
    abstract fun bindLethalsRepository(
        lethalsRepositoryImpl: LethalsRepositoryImpl
    ): LethalsRepository
}
