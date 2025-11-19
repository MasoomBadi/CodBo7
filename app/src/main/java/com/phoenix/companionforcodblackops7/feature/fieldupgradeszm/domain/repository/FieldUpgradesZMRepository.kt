package com.phoenix.companionforcodblackops7.feature.fieldupgradeszm.domain.repository

import com.phoenix.companionforcodblackops7.feature.fieldupgradeszm.domain.model.FieldUpgradeZM
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for Field Upgrades (Zombie mode)
 */
interface FieldUpgradesZMRepository {
    /**
     * Get all field upgrades with their augments
     */
    fun getFieldUpgrades(): Flow<List<FieldUpgradeZM>>
}
