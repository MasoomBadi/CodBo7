package com.phoenix.companionforcodblackops7.feature.feedback.data

import com.phoenix.companionforcodblackops7.feature.feedback.data.remote.FeedbackApiService
import com.phoenix.companionforcodblackops7.feature.feedback.data.repository.FeedbackRepositoryImpl
import com.phoenix.companionforcodblackops7.feature.feedback.domain.repository.FeedbackRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class FeedbackModule {

    @Binds
    @Singleton
    abstract fun bindFeedbackRepository(
        feedbackRepositoryImpl: FeedbackRepositoryImpl
    ): FeedbackRepository

    companion object {
        @Provides
        @Singleton
        fun provideFeedbackApiService(retrofit: Retrofit): FeedbackApiService {
            return retrofit.create(FeedbackApiService::class.java)
        }
    }
}
