package com.phoenix.companionforcodblackops7.feature.fieldupgradeszm.di

import com.phoenix.companionforcodblackops7.feature.fieldupgradeszm.data.repository.FieldUpgradesZMRepositoryImpl
import com.phoenix.companionforcodblackops7.feature.fieldupgradeszm.domain.repository.FieldUpgradesZMRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class FieldUpgradesZMModule {

    @Binds
    @Singleton
    abstract fun bindFieldUpgradesZMRepository(
        impl: FieldUpgradesZMRepositoryImpl
    ): FieldUpgradesZMRepository
}
