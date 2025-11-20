package com.phoenix.companionforcodblackops7.feature.gobblegums.domain.repository

import com.phoenix.companionforcodblackops7.feature.gobblegums.domain.model.GobbleGum
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for GobbleGums
 */
interface GobbleGumsRepository {
    /**
     * Get all gobblegums with their tips, sorted by sort order
     */
    fun getGobbleGums(): Flow<List<GobbleGum>>
}
