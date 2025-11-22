package com.phoenix.companionforcodblackops7.feature.feedback.data.repository

import com.phoenix.companionforcodblackops7.feature.feedback.data.remote.FeedbackApiService
import com.phoenix.companionforcodblackops7.feature.feedback.data.remote.FeedbackRequest
import com.phoenix.companionforcodblackops7.feature.feedback.domain.repository.FeedbackRepository
import com.phoenix.companionforcodblackops7.feature.feedback.domain.repository.FeedbackResult
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FeedbackRepositoryImpl @Inject constructor(
    private val feedbackApiService: FeedbackApiService
) : FeedbackRepository {

    override suspend fun submitFeedback(message: String): FeedbackResult {
        return try {
            val response = feedbackApiService.submitFeedback(FeedbackRequest(message))
            if (response.isSuccessful && response.body()?.success == true) {
                FeedbackResult.Success(response.body()?.message ?: "Feedback submitted successfully")
            } else {
                val errorMessage = response.body()?.message ?: "Failed to submit feedback"
                FeedbackResult.Error(errorMessage)
            }
        } catch (e: Exception) {
            Timber.e(e, "Error submitting feedback")
            FeedbackResult.Error("Network error. Please check your connection and try again.")
        }
    }
}
