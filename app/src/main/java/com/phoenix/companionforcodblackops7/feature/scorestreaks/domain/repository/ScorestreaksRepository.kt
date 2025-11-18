package com.phoenix.companionforcodblackops7.feature.scorestreaks.domain.repository

import com.phoenix.companionforcodblackops7.feature.scorestreaks.domain.model.Scorestreak
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for Scorestreaks data operations
 */
interface ScorestreaksRepository {
    /**
     * Get all scorestreaks sorted by sort order
     */
    fun getAllScorestreaks(): Flow<List<Scorestreak>>

    /**
     * Get a specific scorestreak by ID
     */
    fun getScorestreakById(id: Int): Flow<Scorestreak?>
}
