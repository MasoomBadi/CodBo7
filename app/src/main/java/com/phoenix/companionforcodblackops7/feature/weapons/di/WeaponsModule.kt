package com.phoenix.companionforcodblackops7.feature.weapons.di

import com.phoenix.companionforcodblackops7.feature.weapons.data.repository.WeaponsRepositoryImpl
import com.phoenix.companionforcodblackops7.feature.weapons.domain.repository.WeaponsRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class WeaponsModule {

    @Binds
    @Singleton
    abstract fun bindWeaponsRepository(
        weaponsRepositoryImpl: WeaponsRepositoryImpl
    ): WeaponsRepository
}
