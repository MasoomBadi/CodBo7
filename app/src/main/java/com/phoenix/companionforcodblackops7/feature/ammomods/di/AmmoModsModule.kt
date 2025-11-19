package com.phoenix.companionforcodblackops7.feature.ammomods.di

import com.phoenix.companionforcodblackops7.feature.ammomods.data.repository.AmmoModsRepositoryImpl
import com.phoenix.companionforcodblackops7.feature.ammomods.domain.repository.AmmoModsRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Hilt module for Ammo Mods feature dependencies
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class AmmoModsModule {

    @Binds
    @Singleton
    abstract fun bindAmmoModsRepository(
        ammoModsRepositoryImpl: AmmoModsRepositoryImpl
    ): AmmoModsRepository
}
