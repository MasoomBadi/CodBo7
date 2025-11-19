package com.phoenix.companionforcodblackops7.feature.powerups.domain.model

import androidx.compose.ui.graphics.Color

/**
 * Domain model for Power-Up (Zombie mode)
 */
data class PowerUp(
    val id: Int,
    val name: String,
    val description: String,
    val effectType: String, // "Instant" or "Duration"
    val duration: String?,
    val iconUrl: String,
    val sortOrder: Int
) {
    /**
     * Get accent color for Power-Ups - Gold/Yellow theme
     */
    fun getAccentColor(): Color = Color(0xFFFFC107) // Amber/Gold

    /**
     * Check if this is an instant effect power-up
     */
    fun isInstantEffect(): Boolean = effectType.equals("Instant", ignoreCase = true)

    /**
     * Check if this is a duration-based power-up
     */
    fun isDurationEffect(): Boolean = effectType.equals("Duration", ignoreCase = true)

    /**
     * Get formatted duration text
     */
    fun getDurationText(): String {
        return if (isDurationEffect() && !duration.isNullOrBlank()) {
            duration
        } else {
            "N/A"
        }
    }

    /**
     * Get effect type display text
     */
    fun getEffectTypeText(): String = effectType.uppercase()

    /**
     * Get effect type color
     */
    fun getEffectTypeColor(): Color {
        return if (isInstantEffect()) {
            Color(0xFFFF6F00) // Deep Orange for Instant
        } else {
            Color(0xFF00BFA5) // Teal for Duration
        }
    }
}
