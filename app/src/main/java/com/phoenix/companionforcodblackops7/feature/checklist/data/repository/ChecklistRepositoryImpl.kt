package com.phoenix.companionforcodblackops7.feature.checklist.data.repository

import com.phoenix.companionforcodblackops7.feature.checklist.data.local.ChecklistItemEntity
import com.phoenix.companionforcodblackops7.feature.checklist.domain.model.CategoryProgress
import com.phoenix.companionforcodblackops7.feature.checklist.domain.model.ChecklistCategory
import com.phoenix.companionforcodblackops7.feature.checklist.domain.model.ChecklistItem
import com.phoenix.companionforcodblackops7.feature.checklist.domain.model.ChecklistProgress
import com.phoenix.companionforcodblackops7.feature.checklist.domain.repository.ChecklistRepository
import com.phoenix.companionforcodblackops7.feature.operators.domain.repository.OperatorsRepository
import io.realm.kotlin.Realm
import io.realm.kotlin.ext.query
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class ChecklistRepositoryImpl @Inject constructor(
    private val realm: Realm,
    private val operatorsRepository: OperatorsRepository
) : ChecklistRepository {

    override fun getChecklistItems(category: ChecklistCategory): Flow<List<ChecklistItem>> {
        return when (category) {
            ChecklistCategory.OPERATORS -> {
                // Get operators from repository
                val operatorsFlow = operatorsRepository.getOperators()

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
                            name = operator.name,
                            category = category,
                            isUnlocked = checklistMap[operator.id] ?: false,
                            imageUrl = operator.imageUrl,
                            description = operator.role
                        )
                    }.sortedBy { it.name }
                }
            }
            else -> {
                // For other categories, return empty list for now
                kotlinx.coroutines.flow.flowOf(emptyList())
            }
        }
    }

    override fun getProgress(): Flow<ChecklistProgress> {
        return realm.query<ChecklistItemEntity>().asFlow().map { results ->
            val items = results.list
            val categoryProgressMap = mutableMapOf<ChecklistCategory, CategoryProgress>()

            // For now, only calculate operators progress
            val operatorItems = items.filter { it.category == ChecklistCategory.OPERATORS.name }

            if (operatorItems.isNotEmpty()) {
                val unlockedCount = operatorItems.count { it.isUnlocked }
                categoryProgressMap[ChecklistCategory.OPERATORS] = CategoryProgress(
                    category = ChecklistCategory.OPERATORS,
                    totalItems = operatorItems.size,
                    unlockedItems = unlockedCount
                )
            }

            val totalUnlocked = items.count { it.isUnlocked }
            val totalItems = items.size

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
