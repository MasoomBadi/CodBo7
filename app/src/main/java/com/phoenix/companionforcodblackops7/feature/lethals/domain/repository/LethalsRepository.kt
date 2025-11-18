package com.phoenix.companionforcodblackops7.feature.lethals.domain.repository

import com.phoenix.companionforcodblackops7.feature.lethals.domain.model.Lethal
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for Lethals data operations
 */
interface LethalsRepository {
    /**
     * Get all lethals
     */
    fun getAllLethals(): Flow<List<Lethal>>

    /**
     * Get a specific lethal by ID
     */
    fun getLethalById(id: Int): Flow<Lethal?>
}
