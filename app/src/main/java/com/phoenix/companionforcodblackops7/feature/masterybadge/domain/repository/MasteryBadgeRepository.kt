package com.phoenix.companionforcodblackops7.feature.masterybadge.domain.repository

import com.phoenix.companionforcodblackops7.feature.masterybadge.domain.model.MasteryBadge
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for mastery badge tracking
 * Handles fetching badge requirements from database and managing local completion progress
 */
interface MasteryBadgeRepository {
    /**
     * Get all mastery badges for a specific weapon
     * Returns Flow for reactive updates when user toggles checkboxes
     *
     * @param weaponId The weapon ID to fetch badges for
     * @return Flow of all badges sorted by mode and sort_order, with completion status
     */
    fun getBadgesForWeapon(weaponId: Int): Flow<List<MasteryBadge>>

    /**
     * Toggle completion status for a specific badge
     *
     * @param weaponId The weapon ID
     * @param badgeLevel The badge level (e.g., "badge_1")
     * @param mode The mode (e.g., "multiplayer")
     */
    suspend fun toggleBadgeCompletion(weaponId: Int, badgeLevel: String, mode: String)

    /**
     * Get total badge count and completed count for a weapon
     *
     * @param weaponId The weapon ID
     * @return Pair of (completedCount, totalCount)
     */
    suspend fun getBadgeProgress(weaponId: Int): Pair<Int, Int>
}
