package com.phoenix.companionforcodblackops7.feature.feedback.domain.repository

sealed class FeedbackResult {
    data class Success(val message: String) : FeedbackResult()
    data class Error(val message: String) : FeedbackResult()
}

interface FeedbackRepository {
    suspend fun submitFeedback(message: String): FeedbackResult
}
