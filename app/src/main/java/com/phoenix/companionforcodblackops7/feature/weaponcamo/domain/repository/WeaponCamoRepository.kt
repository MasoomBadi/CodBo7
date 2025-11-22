package com.phoenix.companionforcodblackops7.feature.weaponcamo.domain.repository

import com.phoenix.companionforcodblackops7.feature.weaponcamo.domain.model.Camo
import com.phoenix.companionforcodblackops7.feature.weaponcamo.domain.model.CamoCategory
import com.phoenix.companionforcodblackops7.feature.weaponcamo.domain.model.CamoCriteria
import com.phoenix.companionforcodblackops7.feature.weaponcamo.domain.model.Weapon
import kotlinx.coroutines.flow.Flow

interface WeaponCamoRepository {

    /**
     * Get all weapons with their camo completion progress
     * Grouped by weapon category, sorted by sort_order
     */
    fun getAllWeapons(): Flow<List<Weapon>>

    /**
     * Get a single weapon by ID with progress
     */
    suspend fun getWeapon(weaponId: Int): Weapon?

    /**
     * Get all camos for a weapon in a specific mode
     * Grouped by category, with dependency logic applied
     *
     * @param weaponId The weapon ID
     * @param mode The camo mode (campaign, multiplayer, zombie, prestige)
     * @return List of CamoCategory with camos sorted by category_order and sort_order
     */
    fun getCamosForWeapon(weaponId: Int, mode: String): Flow<List<CamoCategory>>

    /**
     * Get all criteria for a specific camo
     *
     * @param weaponId The weapon ID
     * @param camoId The camo ID
     * @return List of criteria sorted by criteria_order
     */
    suspend fun getCamoCriteria(weaponId: Int, camoId: Int): List<CamoCriteria>

    /**
     * Toggle a criterion's completion state
     *
     * @param weaponId The weapon ID
     * @param camoId The camo ID
     * @param criterionId The criterion ID
     */
    suspend fun toggleCriterion(weaponId: Int, camoId: Int, criterionId: Int)

    /**
     * Check if a camo is unlocked based on dependency logic
     *
     * Rules:
     * 1. Category dependency: All camos in previous category_order must be complete
     * 2. Item dependency: Previous sort_order in same category must be complete
     *
     * @param weaponId The weapon ID
     * @param camo The camo to check
     * @param allCamosInMode All camos for this weapon in the same mode
     * @return true if unlocked, false if locked
     */
    suspend fun isCamoUnlocked(
        weaponId: Int,
        camo: Camo,
        allCamosInMode: List<Camo>
    ): Boolean

    /**
     * Get overall progress for a weapon (X/54)
     *
     * @param weaponId The weapon ID
     * @return Pair of (completed, total)
     */
    suspend fun getWeaponProgress(weaponId: Int): Pair<Int, Int>
}
