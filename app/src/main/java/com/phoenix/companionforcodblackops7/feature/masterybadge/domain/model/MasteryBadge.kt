package com.phoenix.companionforcodblackops7.feature.masterybadge.domain.model

data class MasteryBadge(
    val id: Int,
    val weaponId: Int,
    val badgeLevel: BadgeLevel,
    val mpKillsRequired: Int,
    val zmKillsRequired: Int
)

enum class BadgeLevel(val displayName: String) {
    BADGE_1("Badge I"),
    BADGE_2("Badge II"),
    MASTERY("Mastery");

    companion object {
        fun fromString(value: String): BadgeLevel {
            return when (value.lowercase()) {
                "badge_1" -> BADGE_1
                "badge_2" -> BADGE_2
                "mastery" -> MASTERY
                else -> BADGE_1
            }
        }
    }
}

enum class BadgeMode(val displayName: String) {
    MULTIPLAYER("Multiplayer"),
    ZOMBIE("Zombie");

    companion object {
        fun fromString(value: String): BadgeMode {
            return when (value.lowercase()) {
                "multiplayer", "mp" -> MULTIPLAYER
                "zombie", "zm" -> ZOMBIE
                else -> MULTIPLAYER
            }
        }
    }
}

/**
 * Represents a badge with its unlock status for a specific mode
 */
data class BadgeProgress(
    val badge: MasteryBadge,
    val mode: BadgeMode,
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
    val totalBadges: Int = 6 // Always 6 badges per weapon (3 MP + 3 ZM)

    val unlockedBadgesCount: Int
        get() = mpBadges.count { it.isUnlocked } + zmBadges.count { it.isUnlocked }

    val percentage: Float
        get() = if (totalBadges > 0) (unlockedBadgesCount.toFloat() / totalBadges) * 100 else 0f
}
