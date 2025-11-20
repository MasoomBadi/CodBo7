package com.phoenix.companionforcodblackops7.feature.prestige.domain.repository

import com.phoenix.companionforcodblackops7.feature.prestige.domain.model.PrestigeItem
import kotlinx.coroutines.flow.Flow

interface PrestigeRepository {
    fun getAllPrestigeItems(): Flow<List<PrestigeItem>>
}
