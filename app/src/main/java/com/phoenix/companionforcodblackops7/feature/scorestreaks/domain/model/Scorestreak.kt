package com.phoenix.companionforcodblackops7.feature.scorestreaks.domain.model

import androidx.compose.ui.graphics.Color

/**
 * Domain model representing a Scorestreak reward
 */
data class Scorestreak(
    val id: Int,
    val name: String,
    val displayName: String,
    val description: String,
    val scoreCost: Int,
    val unlockLevel: Int,
    val unlockLabel: String,
    val overclock1: String,
    val overclock2: String,
    val iconUrl: String,
    val iconO1Url: String,
    val iconO2Url: String,
    val sortOrder: Int
) {
    /**
     * Get the accent color for scorestreaks (Blue)
     */
    fun getAccentColor(): Color = Color(0xFF1E88E5)

    /**
     * Check if scorestreak is unlocked by default
     */
    fun isDefault(): Boolean = unlockLevel == 0

    /**
     * Get formatted score cost (e.g., "500 PTS")
     */
    fun getFormattedScoreCost(): String = "$scoreCost PTS"
}
