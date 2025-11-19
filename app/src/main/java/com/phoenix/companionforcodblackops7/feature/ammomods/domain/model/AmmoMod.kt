package com.phoenix.companionforcodblackops7.feature.ammomods.domain.model

import androidx.compose.ui.graphics.Color

/**
 * Domain model representing an Ammo Mod with its augments
 */
data class AmmoMod(
    val id: Int,
    val name: String,
    val description: String,
    val unlockLevel: Int,
    val unlockLabel: String,
    val iconUrl: String,
    val boxUrl: String,
    val recipeUrl: String,
    val sortOrder: Int,
    val augments: List<AmmoModAugment> = emptyList()
) {
    /**
     * Get the accent color for Ammo Mods (Pink/Magenta)
     */
    fun getAccentColor(): Color = Color(0xFFE91E63)

    /**
     * Check if ammo mod is unlocked by default
     */
    fun isDefault(): Boolean = unlockLevel == 0

    /**
     * Get formatted unlock text
     */
    fun getUnlockText(): String {
        return if (isDefault()) "DEFAULT" else unlockLabel.uppercase()
    }

    /**
     * Get all major augments
     */
    fun getMajorAugments(): List<AmmoModAugment> {
        return augments.filter { it.isMajorAugment() }.sortedBy { it.sortOrder }
    }

    /**
     * Get all minor augments
     */
    fun getMinorAugments(): List<AmmoModAugment> {
        return augments.filter { it.isMinorAugment() }.sortedBy { it.sortOrder }
    }

    /**
     * Get total count of augments
     */
    fun getTotalAugmentCount(): Int = augments.size

    /**
     * Get count of major augments
     */
    fun getMajorAugmentCount(): Int = getMajorAugments().size

    /**
     * Get count of minor augments
     */
    fun getMinorAugmentCount(): Int = getMinorAugments().size

    /**
     * Check if ammo mod has augments
     */
    fun hasAugments(): Boolean = augments.isNotEmpty()
}
