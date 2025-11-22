package com.phoenix.companionforcodblackops7.feature.checklist.domain.model

import androidx.compose.ui.graphics.Color

/**
 * Constants for the Collection Tracker feature
 * Centralizes magic numbers, thresholds, and configuration values
 */
object ChecklistConstants {

    // Mastery Badge Kill Thresholds
    object MasteryBadge {
        const val MP_BADGE_1_KILLS = 100
        const val MP_BADGE_2_KILLS = 250
        const val MP_MASTERY_KILLS = 500

        const val ZM_BADGE_1_KILLS = 500
        const val ZM_BADGE_2_KILLS = 1500
        const val ZM_MASTERY_KILLS = 3000

        const val TOTAL_BADGES_PER_WEAPON = 6
        const val BADGES_PER_MODE = 3
    }

    // DataStore Preference Keys
    object PreferenceKeys {
        fun weaponMpKills(weaponId: Int) = "weapon_${weaponId}_mp_kills"
        fun weaponZmKills(weaponId: Int) = "weapon_${weaponId}_zm_kills"
        fun weaponCamoCriterion(weaponId: Int, camoId: Int, criterionId: Int) =
            "weapon_${weaponId}_camo_${camoId}_criterion_${criterionId}"
    }

    // Database Table Names
    object Tables {
        const val WEAPONS_MP = "weapons_mp"
        const val CAMO = "camo"
        const val WEAPON_CAMO = "weapon_camo"
        const val CAMO_CRITERIA = "camo_criteria"
    }

    // Camo Categories
    object CamoCategories {
        const val MILITARY = "military"
        const val SPECIAL = "special"
        const val MASTERY = "mastery"
        const val PRESTIGE_M1 = "prestigem1"
        const val PRESTIGE_M2 = "prestigem2"
        const val PRESTIGE_M3 = "prestigem3"

        val ALL_COMMON = listOf(MILITARY, SPECIAL, MASTERY, PRESTIGE_M1, PRESTIGE_M2, PRESTIGE_M3)
    }

    // Category UI Colors
    object CategoryColors {
        val OPERATORS = Color(0xFFF96800) // COD Orange
        val WEAPONS = Color(0xFF00BCD4) // Cyan
        val MASTERY_BADGES = Color(0xFFFFB300) // Gold
        val MAPS = Color(0xFF76FF03) // Green
        val EQUIPMENT = Color(0xFFE91E63) // Pink
        val PRESTIGE = Color(0xFFFFB300) // Gold

        fun forCategory(category: ChecklistCategory): Color {
            return when (category) {
                ChecklistCategory.OPERATORS -> OPERATORS
                ChecklistCategory.WEAPONS -> WEAPONS
                ChecklistCategory.MASTERY_BADGES -> MASTERY_BADGES
                ChecklistCategory.MAPS -> MAPS
                ChecklistCategory.EQUIPMENT -> EQUIPMENT
                ChecklistCategory.PRESTIGE -> PRESTIGE
            }
        }
    }

    // Default Values
    object Defaults {
        const val DEFAULT_WEAPON_CATEGORY = "Assault Rifle"
    }
}
