package com.phoenix.companionforcodblackops7.feature.checklist.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import com.phoenix.companionforcodblackops7.core.data.local.entity.DynamicEntity
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
    private val prestigeRepository: PrestigeRepository,
    private val dataStore: DataStore<Preferences>
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
            ChecklistCategory.WEAPONS -> {
                // Get all weapons from weapons_mp table with category
                val weaponsFlow = realm.query<DynamicEntity>("tableName == $0", "weapons_mp")
                    .asFlow()
                    .map { results ->
                        results.list.mapNotNull { entity ->
                            try {
                                val data = entity.data
                                // Quadruple: id, name, iconUrl, weaponCategory
                                listOf(
                                    data["id"]?.asInt() ?: 0,
                                    data["display_name"]?.asString() ?: "",
                                    data["icon_url"]?.asString() ?: "",
                                    data["category"]?.asString() ?: "Assault Rifle"
                                )
                            } catch (e: Exception) {
                                null
                            }
                        }.sortedBy { it[1] as String } // Sort by display_name
                    }

                // Combine with DataStore to calculate camo progress
                combine(weaponsFlow, dataStore.data) { weapons, prefs ->
                    weapons.map { weaponData ->
                        val weaponId = weaponData[0] as Int
                        val weaponName = weaponData[1] as String
                        val iconUrl = weaponData[2] as String
                        val weaponCategory = weaponData[3] as String

                        // Calculate unlocked camos for this weapon (out of ~54 total)
                        val unlockedCount = (1..54).count { camoId ->
                            val key = booleanPreferencesKey("weapon_camo_${weaponId}_$camoId")
                            prefs[key] ?: false
                        }

                        ChecklistItem(
                            id = "$weaponId|$weaponCategory", // Store category in ID
                            name = weaponName,
                            category = category,
                            isUnlocked = false, // Not used for weapons - we track camos instead
                            imageUrl = iconUrl,
                            unlockCriteria = "$unlockedCount/54 camos unlocked"
                        )
                    }
                }
            }
            ChecklistCategory.MASTERY_BADGES -> {
                // Get all weapons from weapons_mp table with category
                val weaponsFlow = realm.query<DynamicEntity>("tableName == $0", "weapons_mp")
                    .asFlow()
                    .map { results ->
                        results.list.mapNotNull { entity ->
                            try {
                                val data = entity.data
                                // Quadruple: id, name, iconUrl, weaponCategory
                                listOf(
                                    data["id"]?.asInt() ?: 0,
                                    data["display_name"]?.asString() ?: "",
                                    data["icon_url"]?.asString() ?: "",
                                    data["category"]?.asString() ?: "Assault Rifle"
                                )
                            } catch (e: Exception) {
                                null
                            }
                        }.sortedBy { it[1] as String } // Sort by display_name
                    }

                // Combine with DataStore to calculate badge progress
                combine(weaponsFlow, dataStore.data) { weapons, prefs ->
                    weapons.map { weaponData ->
                        val weaponId = weaponData[0] as Int
                        val weaponName = weaponData[1] as String
                        val iconUrl = weaponData[2] as String
                        val weaponCategory = weaponData[3] as String

                        // Get MP and ZM kills from DataStore
                        val mpKillsKey = intPreferencesKey("weapon_${weaponId}_mp_kills")
                        val zmKillsKey = intPreferencesKey("weapon_${weaponId}_zm_kills")
                        val mpKills = prefs[mpKillsKey] ?: 0
                        val zmKills = prefs[zmKillsKey] ?: 0

                        // Calculate unlocked badges (out of 6 total)
                        // Simplified calculation - count badges where kill requirement is met
                        var unlockedCount = 0
                        if (mpKills >= 100) unlockedCount++
                        if (mpKills >= 250) unlockedCount++
                        if (mpKills >= 500) unlockedCount++
                        if (zmKills >= 500) unlockedCount++
                        if (zmKills >= 1500) unlockedCount++
                        if (zmKills >= 3000) unlockedCount++

                        ChecklistItem(
                            id = "$weaponId|$weaponCategory", // Store category in ID
                            name = weaponName,
                            category = category,
                            isUnlocked = false, // Not used for mastery badges
                            imageUrl = iconUrl,
                            unlockCriteria = "$unlockedCount/6 badges unlocked"
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
        // Combine operators, prestige, weapons, and checklist state
        val operatorsFlow = operatorsRepository.getAllOperators()
        val prestigeFlow = prestigeRepository.getAllPrestigeItems()
        val checklistFlow = realm.query<ChecklistItemEntity>().asFlow().map { results ->
            results.list
        }
        val weaponsFlow = realm.query<DynamicEntity>("tableName == $0", "weapons_mp")
            .asFlow()
            .map { results -> results.list.size }

        return combine(
            operatorsFlow,
            prestigeFlow,
            checklistFlow,
            weaponsFlow,
            dataStore.data
        ) { operators, prestigeItems, checklistItems, weaponCount, prefs ->
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

            // Calculate weapon camos progress
            if (weaponCount > 0) {
                // Approximate 54 camos per weapon
                val totalCamos = weaponCount * 54

                // Count unlocked camos across all weapons (1-29) and all camos (1-54)
                var unlockedCamos = 0
                for (weaponId in 1..weaponCount) {
                    for (camoId in 1..54) {
                        val key = booleanPreferencesKey("weapon_camo_${weaponId}_$camoId")
                        if (prefs[key] == true) {
                            unlockedCamos++
                        }
                    }
                }

                categoryProgressMap[ChecklistCategory.WEAPONS] = CategoryProgress(
                    category = ChecklistCategory.WEAPONS,
                    totalItems = totalCamos,
                    unlockedItems = unlockedCamos
                )
            }

            // Calculate mastery badge progress
            if (weaponCount > 0) {
                // 6 badges per weapon (3 MP + 3 ZM)
                val totalBadges = weaponCount * 6

                // Count unlocked badges across all weapons
                var unlockedBadges = 0
                for (weaponId in 1..weaponCount) {
                    val mpKillsKey = intPreferencesKey("weapon_${weaponId}_mp_kills")
                    val zmKillsKey = intPreferencesKey("weapon_${weaponId}_zm_kills")
                    val mpKills = prefs[mpKillsKey] ?: 0
                    val zmKills = prefs[zmKillsKey] ?: 0

                    // Count MP badges (sequential unlock)
                    if (mpKills >= 100) unlockedBadges++ // Badge I
                    if (mpKills >= 250) unlockedBadges++ // Badge II
                    if (mpKills >= 500) unlockedBadges++ // Mastery

                    // Count ZM badges (sequential unlock)
                    if (zmKills >= 500) unlockedBadges++ // Badge I
                    if (zmKills >= 1500) unlockedBadges++ // Badge II
                    if (zmKills >= 3000) unlockedBadges++ // Mastery
                }

                categoryProgressMap[ChecklistCategory.MASTERY_BADGES] = CategoryProgress(
                    category = ChecklistCategory.MASTERY_BADGES,
                    totalItems = totalBadges,
                    unlockedItems = unlockedBadges
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
