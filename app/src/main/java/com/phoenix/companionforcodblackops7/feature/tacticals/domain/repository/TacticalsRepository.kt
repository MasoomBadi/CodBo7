package com.phoenix.companionforcodblackops7.feature.tacticals.domain.repository

import com.phoenix.companionforcodblackops7.feature.tacticals.domain.model.Tactical
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for Tacticals data operations
 */
interface TacticalsRepository {
    /**
     * Get all tacticals sorted by sort order
     */
    fun getAllTacticals(): Flow<List<Tactical>>

    /**
     * Get a specific tactical by ID
     */
    fun getTacticalById(id: Int): Flow<Tactical?>
}
