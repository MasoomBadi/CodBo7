package com.phoenix.companionforcodblackops7.feature.fieldupgrades.di

import com.phoenix.companionforcodblackops7.feature.fieldupgrades.data.repository.FieldUpgradesRepositoryImpl
import com.phoenix.companionforcodblackops7.feature.fieldupgrades.domain.repository.FieldUpgradesRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Hilt module for Field Upgrades feature dependencies
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class FieldUpgradesModule {

    @Binds
    @Singleton
    abstract fun bindFieldUpgradesRepository(
        fieldUpgradesRepositoryImpl: FieldUpgradesRepositoryImpl
    ): FieldUpgradesRepository
}
