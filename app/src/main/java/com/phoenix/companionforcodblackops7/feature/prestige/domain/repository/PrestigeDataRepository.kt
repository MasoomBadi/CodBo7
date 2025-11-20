package com.phoenix.companionforcodblackops7.feature.prestige.domain.repository

import com.phoenix.companionforcodblackops7.feature.prestige.domain.model.PrestigeData
import kotlinx.coroutines.flow.Flow

/**
 * Repository for fetching prestige data from database
 * Used for displaying prestige information on the info screen
 */
interface PrestigeDataRepository {
    fun getPrestigeData(): Flow<List<PrestigeData>>
}
