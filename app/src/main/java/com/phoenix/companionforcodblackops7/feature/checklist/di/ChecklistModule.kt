package com.phoenix.companionforcodblackops7.feature.checklist.di

import com.phoenix.companionforcodblackops7.feature.checklist.data.repository.ChecklistRepositoryImpl
import com.phoenix.companionforcodblackops7.feature.checklist.domain.repository.ChecklistRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class ChecklistModule {

    @Binds
    @Singleton
    abstract fun bindChecklistRepository(
        impl: ChecklistRepositoryImpl
    ): ChecklistRepository
}
