package com.phoenix.companionforcodblackops7.feature.powerups.di

import com.phoenix.companionforcodblackops7.feature.powerups.data.repository.PowerUpsRepositoryImpl
import com.phoenix.companionforcodblackops7.feature.powerups.domain.repository.PowerUpsRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class PowerUpsModule {

    @Binds
    @Singleton
    abstract fun bindPowerUpsRepository(
        impl: PowerUpsRepositoryImpl
    ): PowerUpsRepository
}
