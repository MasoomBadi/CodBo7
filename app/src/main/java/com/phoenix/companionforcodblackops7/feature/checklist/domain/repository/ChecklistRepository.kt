package com.phoenix.companionforcodblackops7.feature.checklist.domain.repository

import com.phoenix.companionforcodblackops7.feature.checklist.domain.model.ChecklistCategory
import com.phoenix.companionforcodblackops7.feature.checklist.domain.model.ChecklistItem
import com.phoenix.companionforcodblackops7.feature.checklist.domain.model.ChecklistProgress
import kotlinx.coroutines.flow.Flow

interface ChecklistRepository {
    fun getChecklistItems(category: ChecklistCategory): Flow<List<ChecklistItem>>
    fun getProgress(): Flow<ChecklistProgress>
    suspend fun toggleItemUnlocked(itemId: String, category: ChecklistCategory)
    suspend fun isItemUnlocked(itemId: String, category: ChecklistCategory): Boolean
}
