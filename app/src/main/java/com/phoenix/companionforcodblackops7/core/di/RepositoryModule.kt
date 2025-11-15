package com.phoenix.companionforcodblackops7.core.di

import com.phoenix.companionforcodblackops7.feature.operators.data.repository.OperatorsRepositoryImpl
import com.phoenix.companionforcodblackops7.feature.operators.domain.repository.OperatorsRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindOperatorsRepository(
        operatorsRepositoryImpl: OperatorsRepositoryImpl
    ): OperatorsRepository
}
