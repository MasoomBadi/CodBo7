package com.phoenix.companionforcodblackops7.feature.ammomods.domain.model

import androidx.compose.ui.graphics.Color

/**
 * Domain model representing an Ammo Mod Augment
 */
data class AmmoModAugment(
    val id: Int,
    val ammoModId: Int,
    val name: String,
    val type: String,
    val effect: String,
    val sortOrder: Int
) {
    /**
     * Check if this is a major augment
     */
    fun isMajorAugment(): Boolean = type.equals("Major", ignoreCase = true)

    /**
     * Check if this is a minor augment
     */
    fun isMinorAugment(): Boolean = type.equals("Minor", ignoreCase = true)

    /**
     * Get the accent color based on augment type
     * Major = Bluish, Minor = Goldish
     */
    fun getAccentColor(): Color {
        return when {
            isMajorAugment() -> Color(0xFF2196F3) // Blue for Major
            isMinorAugment() -> Color(0xFFFFB300) // Gold for Minor
            else -> Color.Gray
        }
    }

    /**
     * Get type label for display
     */
    fun getTypeLabel(): String {
        return when {
            isMajorAugment() -> "MAJOR AUGMENT"
            isMinorAugment() -> "MINOR AUGMENT"
            else -> type.uppercase()
        }
    }
}
