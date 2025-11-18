package com.phoenix.companionforcodblackops7.feature.combatspecialties.di

import com.phoenix.companionforcodblackops7.feature.combatspecialties.data.repository.CombatSpecialtiesRepositoryImpl
import com.phoenix.companionforcodblackops7.feature.combatspecialties.domain.repository.CombatSpecialtiesRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Hilt module for providing Combat Specialties-related dependencies
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class CombatSpecialtiesModule {

    @Binds
    @Singleton
    abstract fun bindCombatSpecialtiesRepository(
        combatSpecialtiesRepositoryImpl: CombatSpecialtiesRepositoryImpl
    ): CombatSpecialtiesRepository
}
