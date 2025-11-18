package com.phoenix.companionforcodblackops7.feature.fieldupgrades.domain.repository

import com.phoenix.companionforcodblackops7.feature.fieldupgrades.domain.model.FieldUpgrade
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for Field Upgrades data operations
 */
interface FieldUpgradesRepository {
    /**
     * Get all field upgrades
     */
    fun getAllFieldUpgrades(): Flow<List<FieldUpgrade>>

    /**
     * Get a specific field upgrade by ID
     */
    fun getFieldUpgradeById(id: Int): Flow<FieldUpgrade?>
}
