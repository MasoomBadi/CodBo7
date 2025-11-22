package com.phoenix.companionforcodblackops7.feature.feedback.data.remote

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface FeedbackApiService {
    @POST("api/feedback")
    suspend fun submitFeedback(@Body request: FeedbackRequest): Response<FeedbackResponse>
}
