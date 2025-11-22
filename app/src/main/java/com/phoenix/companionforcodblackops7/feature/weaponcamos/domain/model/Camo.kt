package com.phoenix.companionforcodblackops7.feature.weaponcamos.domain.model

/**
 * Camo model - Completely dynamic
 * category and mode come from database - no hardcoded enums
 */
data class Camo(
    val id: Int,
    val name: String,
    val displayName: String,
    val category: String, // Dynamic from database (was CamoCategory enum)
    val categoryDisplayName: String, // Display name for category
    val mode: String, // Dynamic from database (was CamoMode enum)
    val modeDisplayName: String, // Display name for mode
    val camoUrl: String,
    val sortOrder: Int,
    val criteria: List<CamoCriteria> = emptyList(),
    val isUnlocked: Boolean = false
) {
    val completedCriteriaCount: Int
        get() = criteria.count { it.isCompleted }

    val totalCriteriaCount: Int
        get() = criteria.size

    val canUnlock: Boolean
        get() = criteria.isNotEmpty() && criteria.all { it.isCompleted }
}

data class CamoCriteria(
    val id: Int,
    val camoId: Int,
    val criteriaOrder: Int,
    val criteriaText: String,
    val isCompleted: Boolean = false,
    val isLocked: Boolean = false
)

/**
 * Weapon camo progress - uses dynamic String for modes
 * camosByMode map key is now String instead of enum
 */
data class WeaponCamoProgress(
    val weaponId: Int,
    val weaponName: String,
    val camosByMode: Map<String, List<Camo>>, // Dynamic mode strings instead of enum
    val totalCamos: Int,
    val unlockedCount: Int
) {
    val percentage: Float
        get() = if (totalCamos > 0) (unlockedCount.toFloat() / totalCamos) * 100 else 0f
}
