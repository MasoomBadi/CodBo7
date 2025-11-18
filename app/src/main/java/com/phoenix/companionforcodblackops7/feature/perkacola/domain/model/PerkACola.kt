package com.phoenix.companionforcodblackops7.feature.perkacola.domain.model

import androidx.compose.ui.graphics.Color

/**
 * Domain model representing a Perk-a-Cola with its augments
 */
data class PerkACola(
    val id: Int,
    val name: String,
    val displayName: String,
    val location: String,
    val effect: String,
    val iconUrl: String,
    val bottleUrl: String,
    val recipeUrl: String,
    val sortOrder: Int,
    val augments: List<PerkAColaAugment> = emptyList()
) {
    /**
     * Get the accent color for Perk-a-Cola (Zombie green)
     */
    fun getAccentColor(): Color = Color(0xFF76FF03)

    /**
     * Get all major augments
     */
    fun getMajorAugments(): List<PerkAColaAugment> {
        return augments.filter { it.isMajorAugment() }.sortedBy { it.sortOrder }
    }

    /**
     * Get all minor augments
     */
    fun getMinorAugments(): List<PerkAColaAugment> {
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
     * Check if perk has augments
     */
    fun hasAugments(): Boolean = augments.isNotEmpty()

    /**
     * Get formatted location for display
     */
    fun getFormattedLocation(): String = location.uppercase()
}
