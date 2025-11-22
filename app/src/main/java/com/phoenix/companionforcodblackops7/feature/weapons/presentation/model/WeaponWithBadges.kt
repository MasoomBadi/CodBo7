package com.phoenix.companionforcodblackops7.feature.weapons.presentation.model

import com.phoenix.companionforcodblackops7.feature.weapons.domain.model.Weapon

/**
 * UI model combining Weapon data with badge progress
 */
data class WeaponWithBadges(
    val weapon: Weapon,
    val completedBadges: Int,
    val totalBadges: Int
) {
    val badgeProgressText: String
        get() = "$completedBadges/$totalBadges"

    val isFullyCompleted: Boolean
        get() = totalBadges > 0 && completedBadges == totalBadges
}
