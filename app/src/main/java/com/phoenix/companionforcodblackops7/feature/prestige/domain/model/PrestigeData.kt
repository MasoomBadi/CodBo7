package com.phoenix.companionforcodblackops7.feature.prestige.domain.model

/**
 * Prestige data from database (for info screen)
 * Separate from PrestigeItem which is used for collection tracking
 */
data class PrestigeData(
    val id: Int,
    val title: String,
    val unlockBy: String,
    val icon: String
)
