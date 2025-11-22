package com.phoenix.companionforcodblackops7.feature.masterybadge.domain.model

/**
 * Domain model representing a mastery badge requirement from the database
 * Completely dynamic - works with any badge_level, mode, and kills_required values
 */
data class MasteryBadge(
    val id: Int,
    val weaponId: Int,
    val badgeLevel: String, // e.g., "badge_1", "badge_2", "mastery"
    val mode: String, // e.g., "multiplayer", "zombies"
    val killsRequired: Int,
    val sortOrder: Int,
    val isCompleted: Boolean = false
)
