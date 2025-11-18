package com.phoenix.companionforcodblackops7.feature.perkacola.domain.repository

import com.phoenix.companionforcodblackops7.feature.perkacola.domain.model.PerkACola
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for Perk-a-Cola data operations
 */
interface PerkAColaRepository {
    /**
     * Get all Perk-a-Colas with their augments
     */
    fun getAllPerkAColas(): Flow<List<PerkACola>>

    /**
     * Get a specific Perk-a-Cola by ID with its augments
     */
    fun getPerkAColaById(id: Int): Flow<PerkACola?>
}
