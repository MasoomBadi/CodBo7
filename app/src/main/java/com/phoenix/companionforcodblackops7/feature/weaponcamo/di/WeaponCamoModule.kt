package com.phoenix.companionforcodblackops7.feature.weaponcamo.di

import com.phoenix.companionforcodblackops7.feature.weaponcamo.data.repository.WeaponCamoRepositoryImpl
import com.phoenix.companionforcodblackops7.feature.weaponcamo.domain.repository.WeaponCamoRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class WeaponCamoModule {

    @Binds
    @Singleton
    abstract fun bindWeaponCamoRepository(
        impl: WeaponCamoRepositoryImpl
    ): WeaponCamoRepository
}
