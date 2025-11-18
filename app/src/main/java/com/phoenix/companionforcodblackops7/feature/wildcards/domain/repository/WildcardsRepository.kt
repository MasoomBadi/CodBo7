package com.phoenix.companionforcodblackops7.feature.wildcards.domain.repository

import com.phoenix.companionforcodblackops7.feature.wildcards.domain.model.Wildcard
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for Wildcards data operations
 */
interface WildcardsRepository {
    /**
     * Get all wildcards sorted by sort order
     */
    fun getAllWildcards(): Flow<List<Wildcard>>

    /**
     * Get a specific wildcard by ID
     */
    fun getWildcardById(id: Int): Flow<Wildcard?>
}
