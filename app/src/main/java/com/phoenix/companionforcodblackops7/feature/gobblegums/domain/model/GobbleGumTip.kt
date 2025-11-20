package com.phoenix.companionforcodblackops7.feature.gobblegums.domain.model

/**
 * Domain model for GobbleGum tip
 */
data class GobbleGumTip(
    val id: Int,
    val gobblegumId: Int,
    val tip: String,
    val sortOrder: Int
)
