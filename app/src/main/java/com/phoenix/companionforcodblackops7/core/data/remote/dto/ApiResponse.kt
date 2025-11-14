package com.phoenix.companionforcodblackops7.core.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class ApiResponse<T>(
    val success: Boolean,
    val data: T,
    val message: String? = null
)
