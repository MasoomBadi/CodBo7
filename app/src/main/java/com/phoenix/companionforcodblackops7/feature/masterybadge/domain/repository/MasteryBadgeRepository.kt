package com.phoenix.companionforcodblackops7.feature.masterybadge.domain.repository

import com.phoenix.companionforcodblackops7.feature.masterybadge.domain.model.WeaponMasteryProgress
import kotlinx.coroutines.flow.Flow

interface MasteryBadgeRepository {
    /**
     * Get mastery badge progress for a specific weapon
     */
    fun getWeaponMasteryProgress(weaponId: Int, weaponName: String, weaponCategory: String): Flow<WeaponMasteryProgress>

    /**
     * Get all weapons with their mastery badge progress
     */
    fun getAllWeaponsMasteryProgress(): Flow<List<WeaponMasteryProgress>>
}
