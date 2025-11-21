package com.phoenix.companionforcodblackops7.feature.weaponcamos.domain.repository

import com.phoenix.companionforcodblackops7.feature.weaponcamos.domain.model.Camo
import com.phoenix.companionforcodblackops7.feature.weaponcamos.domain.model.WeaponCamoProgress
import kotlinx.coroutines.flow.Flow

interface WeaponCamosRepository {
    /**
     * Get all camos for a specific weapon (common + unique)
     * Returns map grouped by mode
     */
    fun getWeaponCamos(weaponId: Int): Flow<Map<com.phoenix.companionforcodblackops7.feature.weaponcamos.domain.model.CamoMode, List<Camo>>>

    /**
     * Get weapon camo progress with unlock status
     */
    fun getWeaponCamoProgress(weaponId: Int, weaponName: String): Flow<WeaponCamoProgress>
}
