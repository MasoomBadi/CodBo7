package com.phoenix.companionforcodblackops7.feature.perks.domain.repository

import com.phoenix.companionforcodblackops7.feature.perks.domain.model.Perk
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for managing Perk data
 */
interface PerksRepository {
    /**
     * Get all perks from the database
     * @return Flow emitting list of all perks
     */
    fun getAllPerks(): Flow<List<Perk>>

    /**
     * Get perks filtered by slot
     * @param slot The perk slot (1, 2, or 3)
     * @return Flow emitting list of perks for the specified slot
     */
    fun getPerksBySlot(slot: Int): Flow<List<Perk>>

    /**
     * Get perks filtered by category
     * @param category The combat specialty category (enforcer, recon, strategist)
     * @return Flow emitting list of perks for the specified category
     */
    fun getPerksByCategory(category: String): Flow<List<Perk>>

    /**
     * Get a single perk by ID
     * @param id The perk ID
     * @return Flow emitting the perk if found, null otherwise
     */
    fun getPerkById(id: Int): Flow<Perk?>
}
