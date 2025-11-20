package com.phoenix.companionforcodblackops7.feature.gobblegums.di

import com.phoenix.companionforcodblackops7.feature.gobblegums.data.repository.GobbleGumsRepositoryImpl
import com.phoenix.companionforcodblackops7.feature.gobblegums.domain.repository.GobbleGumsRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class GobbleGumsModule {

    @Binds
    @Singleton
    abstract fun bindGobbleGumsRepository(
        impl: GobbleGumsRepositoryImpl
    ): GobbleGumsRepository
}
