package com.phoenix.companionforcodblackops7.feature.fieldupgrades.domain.model

import androidx.compose.ui.graphics.Color

/**
 * Domain model representing a Field Upgrade
 */
data class FieldUpgrade(
    val id: Int,
    val name: String,
    val displayName: String,
    val availableMultiplayer: Boolean,
    val availableZombies: Boolean,
    val unlockLevel: Int,
    val unlockLabel: String,
    val description: String,
    val overclock1: String?,
    val overclock2: String?,
    val iconUrl: String,
    val iconHudUrl: String?,
    val iconO1Url: String?,
    val iconO2Url: String?,
    val sortOrder: Int
) {
    /**
     * Get the accent color for field upgrades (Green)
     */
    fun getAccentColor(): Color = Color(0xFF43A047)

    /**
     * Check if field upgrade is unlocked by default
     */
    fun isDefault(): Boolean = unlockLevel == 0

    /**
     * Check if field upgrade has overclock upgrades
     */
    fun hasOverclocks(): Boolean = !overclock1.isNullOrEmpty() || !overclock2.isNullOrEmpty()

    /**
     * Check if field upgrade has HUD icon
     */
    fun hasHudIcon(): Boolean = !iconHudUrl.isNullOrEmpty()

    /**
     * Get number of available overclocks
     */
    fun getOverclockCount(): Int {
        var count = 0
        if (!overclock1.isNullOrEmpty()) count++
        if (!overclock2.isNullOrEmpty()) count++
        return count
    }

    /**
     * Get availability modes as formatted string
     */
    fun getAvailabilityModes(): String {
        return when {
            availableMultiplayer && availableZombies -> "MP & ZOMBIES"
            availableMultiplayer -> "MULTIPLAYER"
            availableZombies -> "ZOMBIES"
            else -> "UNAVAILABLE"
        }
    }
}
