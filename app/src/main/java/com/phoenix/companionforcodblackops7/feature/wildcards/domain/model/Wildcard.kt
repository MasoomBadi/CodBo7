package com.phoenix.companionforcodblackops7.feature.wildcards.domain.model

import androidx.compose.ui.graphics.Color

/**
 * Domain model representing a Wildcard loadout modifier
 */
data class Wildcard(
    val id: Int,
    val name: String,
    val displayName: String,
    val unlockLevel: Int,
    val unlockLabel: String,
    val description: String,
    val iconUrl: String,
    val sortOrder: Int
) {
    /**
     * Get the accent color for wildcards (Yellow)
     */
    fun getAccentColor(): Color = Color(0xFFFDD835)

    /**
     * Check if wildcard is unlocked by default
     */
    fun isDefault(): Boolean = unlockLevel == 0
}
