package com.phoenix.companionforcodblackops7.feature.weaponcamos.di

import com.phoenix.companionforcodblackops7.feature.weaponcamos.data.repository.WeaponCamosRepositoryImpl
import com.phoenix.companionforcodblackops7.feature.weaponcamos.domain.repository.WeaponCamosRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class WeaponCamosModule {

    @Binds
    @Singleton
    abstract fun bindWeaponCamosRepository(
        weaponCamosRepositoryImpl: WeaponCamosRepositoryImpl
    ): WeaponCamosRepository
}
