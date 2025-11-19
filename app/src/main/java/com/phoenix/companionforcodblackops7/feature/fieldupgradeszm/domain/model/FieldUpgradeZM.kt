package com.phoenix.companionforcodblackops7.feature.fieldupgradeszm.domain.model

import androidx.compose.ui.graphics.Color

/**
 * Domain model for Field Upgrade (Zombie mode)
 */
data class FieldUpgradeZM(
    val id: Int,
    val name: String,
    val description: String,
    val unlockLevel: Int,
    val unlockLabel: String,
    val fireMode: String,
    val maxAmmo: String,
    val iconUrl: String,
    val gunUrl: String,
    val flowUrl: String,
    val sortOrder: Int,
    val augments: List<FieldUpgradeZMAugment> = emptyList()
) {
    /**
     * Get accent color for Field Upgrades (Cyan)
     */
    fun getAccentColor(): Color = Color(0xFF00BCD4) // Cyan

    /**
     * Check if this is a default field upgrade
     */
    fun isDefault(): Boolean = unlockLevel == 0

    /**
     * Get unlock text for display
     */
    fun getUnlockText(): String = if (isDefault()) "DEFAULT" else unlockLabel.uppercase()

    /**
     * Get major augments only
     */
    fun getMajorAugments(): List<FieldUpgradeZMAugment> {
        return augments.filter { it.isMajorAugment() }
            .sortedBy { it.sortOrder }
    }

    /**
     * Get minor augments only
     */
    fun getMinorAugments(): List<FieldUpgradeZMAugment> {
        return augments.filter { it.isMinorAugment() }
            .sortedBy { it.sortOrder }
    }

    /**
     * Get total augment count
     */
    fun getTotalAugmentCount(): Int = augments.size

    /**
     * Check if has augments
     */
    fun hasAugments(): Boolean = augments.isNotEmpty()
}
