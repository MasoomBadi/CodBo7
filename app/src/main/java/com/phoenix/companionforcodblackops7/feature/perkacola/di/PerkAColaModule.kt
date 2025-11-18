package com.phoenix.companionforcodblackops7.feature.perkacola.di

import com.phoenix.companionforcodblackops7.feature.perkacola.data.repository.PerkAColaRepositoryImpl
import com.phoenix.companionforcodblackops7.feature.perkacola.domain.repository.PerkAColaRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Hilt module for Perk-a-Cola feature dependencies
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class PerkAColaModule {

    @Binds
    @Singleton
    abstract fun bindPerkAColaRepository(
        perkAColaRepositoryImpl: PerkAColaRepositoryImpl
    ): PerkAColaRepository
}
