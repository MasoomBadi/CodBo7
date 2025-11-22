package com.phoenix.companionforcodblackops7.feature.checklist.data.model

/**
 * Holds mastery badge kill requirements for a specific weapon
 * Completely dynamic - supports ANY number of badges and modes from database
 *
 * Data structure:
 * - badgeLevels: List of (badgeLevel, mpKills, zmKills) sorted by sort order
 */
internal data class WeaponMasteryBadgeRequirements(
    val weaponId: Int,
    /** List of badge requirements: Triple(badgeLevel, mpKillsRequired, zmKillsRequired) */
    val badgeLevels: List<Triple<String, Int, Int>>
) {
    /**
     * Count how many badges are unlocked based on current kill counts
     * Completely dynamic - works with any number of badge levels
     */
    fun countUnlockedBadges(mpKills: Int, zmKills: Int): Int {
        var mpCount = 0
        var zmCount = 0

        // Count MP badges (sequential - badge_1, badge_2, mastery, etc.)
        for ((_, mpRequired, _) in badgeLevels) {
            if (mpKills >= mpRequired) mpCount++ else break
        }

        // Count ZM badges (sequential)
        for ((_, _, zmRequired) in badgeLevels) {
            if (zmKills >= zmRequired) zmCount++ else break
        }

        return mpCount + zmCount
    }
}
