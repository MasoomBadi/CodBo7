package com.phoenix.companionforcodblackops7.feature.checklist.data.model

/**
 * Holds mastery badge kill requirements for a specific weapon
 * Data fetched from weapon_mastery_badge table
 */
internal data class WeaponMasteryBadgeRequirements(
    val weaponId: Int,
    val mpBadge1: Int,
    val mpBadge2: Int,
    val mpMastery: Int,
    val zmBadge1: Int,
    val zmBadge2: Int,
    val zmMastery: Int
) {
    /**
     * Count how many badges are unlocked based on current kill counts
     */
    fun countUnlockedBadges(mpKills: Int, zmKills: Int): Int {
        var count = 0

        // Multiplayer badges (sequential unlock)
        if (mpKills >= mpBadge1) count++
        if (mpKills >= mpBadge2) count++
        if (mpKills >= mpMastery) count++

        // Zombie badges (sequential unlock)
        if (zmKills >= zmBadge1) count++
        if (zmKills >= zmBadge2) count++
        if (zmKills >= zmMastery) count++

        return count
    }

    companion object {
        /**
         * Default requirements if database has no data
         * These match the original game values
         */
        fun default(weaponId: Int) = WeaponMasteryBadgeRequirements(
            weaponId = weaponId,
            mpBadge1 = 100,
            mpBadge2 = 250,
            mpMastery = 500,
            zmBadge1 = 500,
            zmBadge2 = 1500,
            zmMastery = 3000
        )
    }
}
