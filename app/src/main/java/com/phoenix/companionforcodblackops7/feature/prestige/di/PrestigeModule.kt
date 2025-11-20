package com.phoenix.companionforcodblackops7.feature.prestige.di

import com.phoenix.companionforcodblackops7.feature.prestige.data.repository.PrestigeRepositoryImpl
import com.phoenix.companionforcodblackops7.feature.prestige.domain.repository.PrestigeRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class PrestigeModule {

    @Binds
    @Singleton
    abstract fun bindPrestigeRepository(
        impl: PrestigeRepositoryImpl
    ): PrestigeRepository
}
