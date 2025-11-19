package com.phoenix.companionforcodblackops7.feature.fieldupgradeszm.domain.model

import androidx.compose.ui.graphics.Color

/**
 * Domain model for Field Upgrade (Zombie mode) augment
 */
data class FieldUpgradeZMAugment(
    val id: Int,
    val fieldUpgradeId: Int,
    val name: String,
    val type: String, // "Major" or "Minor"
    val effect: String,
    val sortOrder: Int
) {
    /**
     * Check if augment is Major type
     */
    fun isMajorAugment(): Boolean = type.equals("Major", ignoreCase = true)

    /**
     * Check if augment is Minor type
     */
    fun isMinorAugment(): Boolean = type.equals("Minor", ignoreCase = true)

    /**
     * Get color based on augment type
     * Major = Blue, Minor = Gold
     */
    fun getTypeColor(): Color {
        return when {
            isMajorAugment() -> Color(0xFF2196F3) // Blue
            isMinorAugment() -> Color(0xFFFFB300) // Gold
            else -> Color.Gray
        }
    }

    /**
     * Get display label for type
     */
    fun getTypeLabel(): String = type.uppercase()
}
