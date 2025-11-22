package com.phoenix.companionforcodblackops7.feature.checklist.domain.model

import androidx.compose.ui.graphics.Color

/**
 * Constants for the Collection Tracker feature
 *
 * IMPORTANT: This file contains ONLY truly static values that never change:
 * - DataStore preference key patterns
 * - Database table names (schema)
 * - UI colors (presentation layer preferences)
 *
 * ALL data values (counts, categories, thresholds) are fetched dynamically from database.
 */
object ChecklistConstants {

    // DataStore Preference Keys (patterns - not data)
    object PreferenceKeys {
        fun weaponMpKills(weaponId: Int) = "weapon_${weaponId}_mp_kills"
        fun weaponZmKills(weaponId: Int) = "weapon_${weaponId}_zm_kills"
        fun weaponCamoCriterion(weaponId: Int, camoId: Int, criterionId: Int) =
            "weapon_${weaponId}_camo_${camoId}_criterion_${criterionId}"
    }

    // Database Table Names (schema - rarely changes)
    object Tables {
        const val WEAPONS_MP = "weapons_mp"
        const val CAMO = "camo"
        const val WEAPON_CAMO = "weapon_camo"
        const val CAMO_CRITERIA = "camo_criteria"
        const val CLASSIC_PRESTIGE = "classic_prestige"
    }

    // UI Category Colors (presentation layer - not data)
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
}
