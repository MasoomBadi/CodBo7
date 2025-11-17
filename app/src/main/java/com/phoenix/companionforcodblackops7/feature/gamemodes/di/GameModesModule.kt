package com.phoenix.companionforcodblackops7.feature.gamemodes.di

import com.phoenix.companionforcodblackops7.feature.gamemodes.data.repository.GameModesRepositoryImpl
import com.phoenix.companionforcodblackops7.feature.gamemodes.domain.repository.GameModesRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class GameModesModule {

    @Binds
    @Singleton
    abstract fun bindGameModesRepository(
        gameModesRepositoryImpl: GameModesRepositoryImpl
    ): GameModesRepository
}
