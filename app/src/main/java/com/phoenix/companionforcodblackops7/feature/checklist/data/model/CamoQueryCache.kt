package com.phoenix.companionforcodblackops7.feature.checklist.data.model

/**
 * Cache object for weapon camo query results
 * Optimizes performance by querying all data once and processing in memory
 */
internal data class CamoQueryCache(
    /** Map of weaponId to list of ALL camo IDs (from weapon_camo junction table) */
    val weaponCamoMap: Map<Int, List<Int>>,

    /** Map of (weaponId, camoId) to list of criterion IDs */
    val criteriaMap: Map<Pair<Int, Int>, List<Int>>
) {
    /**
     * Get all camo IDs for a specific weapon from weapon_camo junction table
     */
    fun getCamoIdsForWeapon(weaponId: Int): Set<Int> {
        return (weaponCamoMap[weaponId] ?: emptyList()).toSet()
    }

    /**
     * Get all criterion IDs for a specific camo on a specific weapon
     */
    fun getCriteriaForCamo(weaponId: Int, camoId: Int): List<Int>? {
        return criteriaMap[weaponId to camoId]
    }
}
