package com.phoenix.companionforcodblackops7.feature.tacticals.domain.model

import androidx.compose.ui.graphics.Color

/**
 * Domain model representing a Tactical equipment
 */
data class Tactical(
    val id: Int,
    val name: String,
    val displayName: String,
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
     * Get the accent color for tacticals (Teal/Cyan)
     */
    fun getAccentColor(): Color = Color(0xFF26A69A)

    /**
     * Check if tactical is unlocked by default
     */
    fun isDefault(): Boolean = unlockLevel == 0

    /**
     * Check if tactical has overclock upgrades
     */
    fun hasOverclocks(): Boolean = !overclock1.isNullOrEmpty() || !overclock2.isNullOrEmpty()

    /**
     * Check if tactical has HUD icon
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
}
