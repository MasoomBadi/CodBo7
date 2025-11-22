package com.phoenix.companionforcodblackops7.feature.masterybadge.domain.model

/**
 * Mastery badge model - Completely dynamic
 * badge_level comes from database (e.g., "badge_1", "badge_2", "mastery", etc.)
 * Supports ANY number of badge levels - no hardcoded enum
 */
data class MasteryBadge(
    val id: Int,
    val weaponId: Int,
    val badgeLevel: String, // Dynamic from database (was enum)
    val badgeLevelDisplayName: String, // Display name from database
    val sortOrder: Int, // For sorting badges in correct order
    val mpKillsRequired: Int,
    val zmKillsRequired: Int
)

/**
 * Represents a badge with its unlock status for a specific mode
 * mode is now dynamic String from database (was enum)
 */
data class BadgeProgress(
    val badge: MasteryBadge,
    val mode: String, // Dynamic from database: "mp", "zm", etc. (was enum)
    val modeDisplayName: String, // Display name for the mode
    val currentKills: Int,
    val requiredKills: Int,
    val isUnlocked: Boolean,
    val isLocked: Boolean // Locked if previous badges not unlocked
) {
    val progress: Float
        get() = if (requiredKills > 0) (currentKills.toFloat() / requiredKills).coerceIn(0f, 1f) else 0f

    val percentage: Int
        get() = (progress * 100).toInt()
}

/**
 * Represents all mastery badge progress for a single weapon
 */
data class WeaponMasteryProgress(
    val weaponId: Int,
    val weaponName: String,
    val weaponCategory: String,
    val mpKills: Int,
    val zmKills: Int,
    val mpBadges: List<BadgeProgress>,
    val zmBadges: List<BadgeProgress>
) {
    val totalBadges: Int
        get() = 6 // Static: 3 MP badges + 3 Zombie badges = 6 total

    val unlockedBadgesCount: Int
        get() = mpBadges.count { it.isUnlocked } + zmBadges.count { it.isUnlocked }

    val percentage: Float
        get() = if (totalBadges > 0) (unlockedBadgesCount.toFloat() / totalBadges) * 100 else 0f
}
