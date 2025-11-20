package com.phoenix.companionforcodblackops7.feature.checklist.data.repository

import com.phoenix.companionforcodblackops7.feature.checklist.data.local.ChecklistItemEntity
import com.phoenix.companionforcodblackops7.feature.checklist.domain.model.CategoryProgress
import com.phoenix.companionforcodblackops7.feature.checklist.domain.model.ChecklistCategory
import com.phoenix.companionforcodblackops7.feature.checklist.domain.model.ChecklistItem
import com.phoenix.companionforcodblackops7.feature.checklist.domain.model.ChecklistProgress
import com.phoenix.companionforcodblackops7.feature.checklist.domain.repository.ChecklistRepository
import com.phoenix.companionforcodblackops7.feature.operators.domain.repository.OperatorsRepository
import com.phoenix.companionforcodblackops7.feature.prestige.domain.repository.PrestigeRepository
import io.realm.kotlin.Realm
import io.realm.kotlin.ext.query
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class ChecklistRepositoryImpl @Inject constructor(
    private val realm: Realm,
    private val operatorsRepository: OperatorsRepository,
    private val prestigeRepository: PrestigeRepository
) : ChecklistRepository {

    override fun getChecklistItems(category: ChecklistCategory): Flow<List<ChecklistItem>> {
        return when (category) {
            ChecklistCategory.OPERATORS -> {
                // Get operators from repository
                val operatorsFlow = operatorsRepository.getAllOperators()

                // Get checklist state from realm
                val checklistFlow = realm.query<ChecklistItemEntity>(
                    "category == $0", category.name
                ).asFlow().map { results ->
                    results.list.associate { it.id to it.isUnlocked }
                }

                // Combine both flows
                combine(operatorsFlow, checklistFlow) { operators, checklistMap ->
                    operators.map { operator ->
                        ChecklistItem(
                            id = operator.id,
                            name = operator.fullName,
                            category = category,
                            isUnlocked = checklistMap[operator.id] ?: false,
                            imageUrl = operator.imageUrl,
                            unlockCriteria = operator.unlockCriteria
                        )
                    }.sortedBy { it.name }
                }
            }
            ChecklistCategory.PRESTIGE -> {
                // Get prestige items from repository
                val prestigeFlow = prestigeRepository.getAllPrestigeItems()

                // Get checklist state from realm
                val checklistFlow = realm.query<ChecklistItemEntity>(
                    "category == $0", category.name
                ).asFlow().map { results ->
                    results.list.associate { it.id to it.isUnlocked }
                }

                // Combine both flows
                combine(prestigeFlow, checklistFlow) { prestigeItems, checklistMap ->
                    prestigeItems.map { item ->
                        ChecklistItem(
                            id = item.id,
                            name = item.name,
                            category = category,
                            isUnlocked = checklistMap[item.id] ?: false,
                            imageUrl = null,
                            unlockCriteria = item.description
                        )
                    }
                }
            }
            else -> {
                // For other categories, return empty list for now
                kotlinx.coroutines.flow.flowOf(emptyList())
            }
        }
    }

    override fun getProgress(): Flow<ChecklistProgress> {
        // Combine operators and prestige data with checklist state
        val operatorsFlow = operatorsRepository.getAllOperators()
        val prestigeFlow = prestigeRepository.getAllPrestigeItems()
        val checklistFlow = realm.query<ChecklistItemEntity>().asFlow().map { results ->
            results.list
        }

        return combine(operatorsFlow, prestigeFlow, checklistFlow) { operators, prestigeItems, checklistItems ->
            val categoryProgressMap = mutableMapOf<ChecklistCategory, CategoryProgress>()

            // Calculate operators progress
            if (operators.isNotEmpty()) {
                val operatorChecklistMap = checklistItems
                    .filter { it.category == ChecklistCategory.OPERATORS.name }
                    .associate { it.id to it.isUnlocked }

                val unlockedCount = operators.count { operator ->
                    operatorChecklistMap[operator.id] == true
                }

                categoryProgressMap[ChecklistCategory.OPERATORS] = CategoryProgress(
                    category = ChecklistCategory.OPERATORS,
                    totalItems = operators.size,
                    unlockedItems = unlockedCount
                )
            }

            // Calculate prestige progress
            if (prestigeItems.isNotEmpty()) {
                val prestigeChecklistMap = checklistItems
                    .filter { it.category == ChecklistCategory.PRESTIGE.name }
                    .associate { it.id to it.isUnlocked }

                val unlockedCount = prestigeItems.count { item ->
                    prestigeChecklistMap[item.id] == true
                }

                categoryProgressMap[ChecklistCategory.PRESTIGE] = CategoryProgress(
                    category = ChecklistCategory.PRESTIGE,
                    totalItems = prestigeItems.size,
                    unlockedItems = unlockedCount
                )
            }

            val totalItems = categoryProgressMap.values.sumOf { it.totalItems }
            val totalUnlocked = categoryProgressMap.values.sumOf { it.unlockedItems }

            ChecklistProgress(
                totalItems = totalItems,
                unlockedItems = totalUnlocked,
                categoryProgress = categoryProgressMap
            )
        }
    }

    override suspend fun toggleItemUnlocked(itemId: String, category: ChecklistCategory) {
        realm.write {
            val existing = query<ChecklistItemEntity>("id == $0", itemId).first().find()

            if (existing != null) {
                existing.isUnlocked = !existing.isUnlocked
            } else {
                copyToRealm(ChecklistItemEntity().apply {
                    id = itemId
                    this.category = category.name
                    isUnlocked = true
                })
            }
        }
    }

    override suspend fun isItemUnlocked(itemId: String, category: ChecklistCategory): Boolean {
        return realm.query<ChecklistItemEntity>("id == $0", itemId)
            .first()
            .find()
            ?.isUnlocked ?: false
    }
}
