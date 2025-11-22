package com.phoenix.companionforcodblackops7.feature.feedback.data.remote

import kotlinx.serialization.Serializable

@Serializable
data class FeedbackRequest(
    val message: String
)

@Serializable
data class FeedbackResponse(
    val success: Boolean,
    val data: FeedbackData? = null,
    val message: String
)

@Serializable
data class FeedbackData(
    val id: Int
)
