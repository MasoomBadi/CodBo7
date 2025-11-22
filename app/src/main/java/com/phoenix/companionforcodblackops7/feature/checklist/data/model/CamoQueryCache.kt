package com.phoenix.companionforcodblackops7.feature.checklist.data.model

/**
 * Cache object for weapon camo query results
 * Optimizes performance by querying all data once and processing in memory
 */
internal data class CamoQueryCache(
    /** Common camo IDs shared by all weapons */
    val commonCamoIds: Set<Int>,

    /** Map of weaponId to list of unique camo IDs */
    val weaponCamoMap: Map<Int, List<Int>>,

    /** Map of (weaponId, camoId) to list of criterion IDs */
    val criteriaMap: Map<Pair<Int, Int>, List<Int>>
) {
    /**
     * Get all camo IDs for a specific weapon (common + unique)
     */
    fun getCamoIdsForWeapon(weaponId: Int): Set<Int> {
        return commonCamoIds + (weaponCamoMap[weaponId] ?: emptyList())
    }

    /**
     * Get all criterion IDs for a specific camo on a specific weapon
     */
    fun getCriteriaForCamo(weaponId: Int, camoId: Int): List<Int>? {
        return criteriaMap[weaponId to camoId]
    }
}
