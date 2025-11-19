package com.phoenix.companionforcodblackops7.feature.powerups.domain.repository

import com.phoenix.companionforcodblackops7.feature.powerups.domain.model.PowerUp
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for Power-Ups (Zombie mode)
 */
interface PowerUpsRepository {
    /**
     * Get all power-ups sorted by sort order
     */
    fun getPowerUps(): Flow<List<PowerUp>>
}
