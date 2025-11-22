package com.phoenix.companionforcodblackops7.feature.checklist.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import com.phoenix.companionforcodblackops7.core.data.local.entity.DynamicEntity
import com.phoenix.companionforcodblackops7.feature.checklist.data.local.ChecklistItemEntity
import com.phoenix.companionforcodblackops7.feature.checklist.data.model.CamoQueryCache
import com.phoenix.companionforcodblackops7.feature.checklist.domain.model.CategoryProgress
import com.phoenix.companionforcodblackops7.feature.checklist.domain.model.ChecklistCategory
import com.phoenix.companionforcodblackops7.feature.checklist.domain.model.ChecklistConstants
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
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Implementation of ChecklistRepository following clean architecture principles
 *
 * Responsibilities:
 * - Aggregate checklist items from multiple sources (operators, prestige, weapons, camos)
 * - Calculate progress across all collection categories
 * - Manage unlock state persistence via Realm and DataStore
 *
 * Performance optimizations:
 * - Bulk query camo data once, process in memory (prevents N+1 queries)
 * - Use Flow for reactive updates
 * - Cache common camo IDs shared across weapons
 */
@Singleton
class ChecklistRepositoryImpl @Inject constructor(
    private val realm: Realm,
    private val operatorsRepository: OperatorsRepository,
    private val prestigeRepository: PrestigeRepository,
    private val dataStore: DataStore<Preferences>,
    private val masteryBadgeRepository: com.phoenix.companionforcodblackops7.feature.masterybadge.domain.repository.MasteryBadgeRepository
) : ChecklistRepository {

    // =============================================================================================
    // Public API
    // =============================================================================================

    override fun getChecklistItems(category: ChecklistCategory): Flow<List<ChecklistItem>> {
        return when (category) {
            ChecklistCategory.OPERATORS -> getOperatorItems()
            ChecklistCategory.PRESTIGE -> getPrestigeItems()
            ChecklistCategory.WEAPONS -> getWeaponCamoItems()
            ChecklistCategory.MASTERY_BADGES -> getMasteryBadgeItems()
            ChecklistCategory.MAPS, ChecklistCategory.EQUIPMENT -> kotlinx.coroutines.flow.flowOf(emptyList())
        }
    }

    override fun getProgress(): Flow<ChecklistProgress> {
        val operatorsFlow = operatorsRepository.getAllOperators()
        val prestigeFlow = prestigeRepository.getAllPrestigeItems()
        val checklistFlow = realm.query<ChecklistItemEntity>().asFlow().map { it.list }
        val weaponsFlow = getWeaponEntities().map { it.size }

        return combine(
            operatorsFlow,
            prestigeFlow,
            checklistFlow,
            weaponsFlow,
            dataStore.data
        ) { operators, prestigeItems, checklistItems, weaponCount, prefs ->
            val categoryProgressMap = mutableMapOf<ChecklistCategory, CategoryProgress>()

            // Calculate progress for each category
            calculateOperatorProgress(operators, checklistItems, categoryProgressMap)
            calculatePrestigeProgress(prestigeItems, checklistItems, categoryProgressMap)
            calculateWeaponCamoProgress(weaponCount, prefs, categoryProgressMap)
            calculateMasteryBadgeProgress(weaponCount, prefs, categoryProgressMap)

            // Aggregate overall progress
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
        try {
            // Use compound key: "CATEGORY_ID" to avoid conflicts (Realm doesn't support composite PKs)
            val compoundKey = "${category.name}_$itemId"
            Timber.d("Toggling item: id=$itemId, category=${category.name}, compoundKey=$compoundKey")

            realm.write {
                val existing = query<ChecklistItemEntity>("id == $0", compoundKey).first().find()

                if (existing != null) {
                    existing.isUnlocked = !existing.isUnlocked
                    Timber.d("Toggled existing item $compoundKey: ${existing.isUnlocked}")
                } else {
                    copyToRealm(ChecklistItemEntity().apply {
                        this.id = compoundKey
                        this.category = category.name
                        this.isUnlocked = true
                    })
                    Timber.d("Created new unlocked item: $compoundKey (category=${category.name})")
                }
            }
        } catch (e: Exception) {
            Timber.e(e, "Failed to toggle item unlock: $itemId")
            throw e
        }
    }

    override suspend fun isItemUnlocked(itemId: String, category: ChecklistCategory): Boolean {
        return try {
            // Use compound key: "CATEGORY_ID"
            val compoundKey = "${category.name}_$itemId"
            realm.query<ChecklistItemEntity>("id == $0", compoundKey)
                .first()
                .find()
                ?.isUnlocked ?: false
        } catch (e: Exception) {
            Timber.e(e, "Failed to check item unlock status: $itemId")
            false
        }
    }

    // =============================================================================================
    // Operator Items
    // =============================================================================================

    private fun getOperatorItems(): Flow<List<ChecklistItem>> {
        val operatorsFlow = operatorsRepository.getAllOperators()
        val checklistFlow = getChecklistMap(ChecklistCategory.OPERATORS)

        return combine(operatorsFlow, checklistFlow) { operators, checklistMap ->
            operators.map { operator ->
                ChecklistItem(
                    id = operator.id,
                    name = operator.fullName,
                    category = ChecklistCategory.OPERATORS,
                    isUnlocked = checklistMap[operator.id] ?: false,
                    imageUrl = operator.imageUrl,
                    unlockCriteria = operator.unlockCriteria
                )
            }.sortedBy { it.name }
        }
    }

    // =============================================================================================
    // Prestige Items
    // =============================================================================================

    private fun getPrestigeItems(): Flow<List<ChecklistItem>> {
        val prestigeFlow = prestigeRepository.getAllPrestigeItems()
        val checklistFlow = getChecklistMap(ChecklistCategory.PRESTIGE)

        return combine(prestigeFlow, checklistFlow) { prestigeItems, checklistMap ->
            Timber.d("Prestige items from DB: ${prestigeItems.size}")
            Timber.d("Checklist map entries: ${checklistMap.size}, keys: ${checklistMap.keys}")

            prestigeItems.map { item ->
                val isUnlocked = checklistMap[item.id] ?: false
                Timber.d("Prestige item ${item.id} (${item.name}): unlocked=$isUnlocked")

                ChecklistItem(
                    id = item.id,
                    name = item.name,
                    category = ChecklistCategory.PRESTIGE,
                    isUnlocked = isUnlocked,
                    imageUrl = null,
                    unlockCriteria = item.description
                )
            }
        }
    }

    // =============================================================================================
    // Weapon Camo Items
    // =============================================================================================

    private fun getWeaponCamoItems(): Flow<List<ChecklistItem>> {
        val weaponsFlow = getWeaponData()

        return combine(weaponsFlow, dataStore.data) { weapons, prefs ->
            // Query all camo data once for performance
            val camoCache = buildCamoQueryCache()

            // Process each weapon in memory
            weapons.map { weaponData ->
                val weaponId = weaponData[0] as Int
                val weaponName = weaponData[1] as String
                val iconUrl = weaponData[2] as String
                val weaponCategory = weaponData[3] as String

                val allCamoIds = camoCache.getCamoIdsForWeapon(weaponId)
                val unlockedCount = countUnlockedCamos(weaponId, allCamoIds, camoCache, prefs)

                ChecklistItem(
                    id = "$weaponId|$weaponCategory",
                    name = weaponName,
                    category = ChecklistCategory.WEAPONS,
                    isUnlocked = false, // Not applicable for weapons
                    imageUrl = iconUrl,
                    unlockCriteria = "$unlockedCount/${allCamoIds.size} camos unlocked"
                )
            }
        }
    }

    // =============================================================================================
    // Mastery Badge Items
    // =============================================================================================

    private fun getMasteryBadgeItems(): Flow<List<ChecklistItem>> {
        val weaponsFlow = getWeaponData()
        val badgeChangesFlow = masteryBadgeRepository.observeAllBadgeChanges()

        return combine(weaponsFlow, badgeChangesFlow) { weapons, _ ->
            weapons.map { weaponData ->
                val weaponId = weaponData[0] as Int
                val weaponName = weaponData[1] as String
                val iconUrl = weaponData[2] as String
                val weaponCategory = weaponData[3] as String

                // Get progress from the new repository
                val (completedCount, totalCount) = kotlinx.coroutines.runBlocking {
                    masteryBadgeRepository.getBadgeProgress(weaponId)
                }

                ChecklistItem(
                    id = "$weaponId|$weaponCategory",
                    name = weaponName,
                    category = ChecklistCategory.MASTERY_BADGES,
                    isUnlocked = completedCount == totalCount && totalCount > 0, // Unlocked when all badges completed
                    imageUrl = iconUrl,
                    unlockCriteria = "$completedCount/$totalCount badges unlocked"
                )
            }
        }
    }

    // =============================================================================================
    // Progress Calculations
    // =============================================================================================

    private fun calculateOperatorProgress(
        operators: List<com.phoenix.companionforcodblackops7.feature.operators.domain.model.Operator>,
        checklistItems: List<ChecklistItemEntity>,
        progressMap: MutableMap<ChecklistCategory, CategoryProgress>
    ) {
        if (operators.isEmpty()) return

        val operatorChecklistMap = checklistItems
            .filter { entity -> entity.category == ChecklistCategory.OPERATORS.name }
            .associate { entity ->
                // Strip compound key prefix: "OPERATORS_ID" -> "ID"
                val originalId = entity.id.removePrefix("${ChecklistCategory.OPERATORS.name}_")
                originalId to entity.isUnlocked
            }

        val unlockedCount = operators.count { operatorChecklistMap[it.id] == true }

        progressMap[ChecklistCategory.OPERATORS] = CategoryProgress(
            category = ChecklistCategory.OPERATORS,
            totalItems = operators.size,
            unlockedItems = unlockedCount
        )
    }

    private fun calculatePrestigeProgress(
        prestigeItems: List<com.phoenix.companionforcodblackops7.feature.prestige.domain.model.PrestigeItem>,
        checklistItems: List<ChecklistItemEntity>,
        progressMap: MutableMap<ChecklistCategory, CategoryProgress>
    ) {
        if (prestigeItems.isEmpty()) return

        val prestigeChecklistMap = checklistItems
            .filter { entity -> entity.category == ChecklistCategory.PRESTIGE.name }
            .associate { entity ->
                // Strip compound key prefix: "PRESTIGE_ID" -> "ID"
                val originalId = entity.id.removePrefix("${ChecklistCategory.PRESTIGE.name}_")
                originalId to entity.isUnlocked
            }

        val unlockedCount = prestigeItems.count { prestigeChecklistMap[it.id] == true }

        progressMap[ChecklistCategory.PRESTIGE] = CategoryProgress(
            category = ChecklistCategory.PRESTIGE,
            totalItems = prestigeItems.size,
            unlockedItems = unlockedCount
        )
    }

    private fun calculateWeaponCamoProgress(
        weaponCount: Int,
        prefs: Preferences,
        progressMap: MutableMap<ChecklistCategory, CategoryProgress>
    ) {
        if (weaponCount == 0) return

        try {
            val weapons = getWeaponEntitiesSync()
            Timber.d("Weapon Camo Progress - Total weapons: ${weapons.size}")

            // OPTIMAL: Query ALL camos once (138 rows), then group in memory
            val allCamos = realm.query<DynamicEntity>(
                "tableName == $0",
                ChecklistConstants.Tables.CAMO
            ).find()

            // Group by mode in memory (microseconds vs milliseconds for DB queries)
            val camosByMode = allCamos.groupBy {
                it.data["mode"]?.asString() ?: ""
            }.mapValues { (_, camos) ->
                camos.mapNotNull { it.data["id"]?.asInt() }
            }

            val campaignCamos = camosByMode["campaign"] ?: emptyList()
            val multiplayerCamos = camosByMode["multiplayer"] ?: emptyList()
            val zombieCamos = camosByMode["zombie"] ?: emptyList()

            // Filter prestige common camos (prestigem1, prestigem2, prestigem3)
            val commonPrestigeCamos = allCamos
                .filter {
                    val category = it.data["category"]?.asString()
                    category in listOf("prestigem1", "prestigem2", "prestigem3")
                }
                .mapNotNull { it.data["id"]?.asInt() }

            Timber.d("Weapon Camo Progress - Camos per mode: campaign=${campaignCamos.size}, multiplayer=${multiplayerCamos.size}, zombie=${zombieCamos.size}, commonPrestige=${commonPrestigeCamos.size}")

            // Query all weapon-specific prestige mappings ONCE (1 query instead of 29)
            val weaponPrestigeMap = realm.query<DynamicEntity>(
                "tableName == $0",
                ChecklistConstants.Tables.WEAPON_CAMO
            ).find()
                .mapNotNull { entity ->
                    val weaponId = entity.data["weapon_id"]?.asInt()
                    val camoId = entity.data["camo_id"]?.asInt()
                    if (weaponId != null && camoId != null) weaponId to camoId else null
                }
                .groupBy({ it.first }, { it.second })

            // Query all criteria ONCE (1 query instead of thousands)
            val allCriteriaMap = realm.query<DynamicEntity>(
                "tableName == $0",
                ChecklistConstants.Tables.CAMO_CRITERIA
            ).find()
                .mapNotNull { entity ->
                    val weaponId = entity.data["weapon_id"]?.asInt()
                    val camoId = entity.data["camo_id"]?.asInt()
                    val criterionId = entity.data["id"]?.asInt()
                    if (weaponId != null && camoId != null && criterionId != null) {
                        Triple(weaponId, camoId, criterionId)
                    } else null
                }
                .groupBy({ it.first to it.second }, { it.third })

            // Calculate totals
            var totalCamosCount = 0
            var unlockedCamosCount = 0
            var totalPrestigeCamosCount = 0

            // Process each weapon in memory
            for (weaponEntity in weapons) {
                val weaponId = weaponEntity.data["id"]?.asInt() ?: continue

                // Campaign camos (all weapons get all campaign camos)
                totalCamosCount += campaignCamos.size
                unlockedCamosCount += countCompletedCamos(weaponId, campaignCamos, allCriteriaMap, prefs)

                // Multiplayer camos (all weapons get all multiplayer camos)
                totalCamosCount += multiplayerCamos.size
                unlockedCamosCount += countCompletedCamos(weaponId, multiplayerCamos, allCriteriaMap, prefs)

                // Zombie camos (all weapons get all zombie camos)
                totalCamosCount += zombieCamos.size
                unlockedCamosCount += countCompletedCamos(weaponId, zombieCamos, allCriteriaMap, prefs)

                // Prestige camos (weapon-specific + common)
                val weaponPrestigeCamos = (weaponPrestigeMap[weaponId] ?: emptyList()) + commonPrestigeCamos
                totalCamosCount += weaponPrestigeCamos.size
                totalPrestigeCamosCount += weaponPrestigeCamos.size
                unlockedCamosCount += countCompletedCamos(weaponId, weaponPrestigeCamos, allCriteriaMap, prefs)
            }

            val expectedTotal = (campaignCamos.size + multiplayerCamos.size + zombieCamos.size) * weapons.size + totalPrestigeCamosCount
            Timber.d("Weapon Camo Progress - Expected: ${weapons.size} weapons × 54 camos = ${weapons.size * 54}")
            Timber.d("Weapon Camo Progress - Calculated: campaign=${campaignCamos.size * weapons.size}, multiplayer=${multiplayerCamos.size * weapons.size}, zombie=${zombieCamos.size * weapons.size}, prestige=$totalPrestigeCamosCount")
            Timber.d("Weapon Camo Progress - Total: $totalCamosCount (expected: $expectedTotal)")
            Timber.d("Weapon Camo Progress - Unlocked: $unlockedCamosCount/$totalCamosCount")

            progressMap[ChecklistCategory.WEAPONS] = CategoryProgress(
                category = ChecklistCategory.WEAPONS,
                totalItems = totalCamosCount,
                unlockedItems = unlockedCamosCount
            )
        } catch (e: Exception) {
            Timber.e(e, "Failed to calculate weapon camo progress")
            // Don't add to progress map if calculation fails
        }
    }

    private fun calculateMasteryBadgeProgress(
        weaponCount: Int,
        prefs: Preferences,
        progressMap: MutableMap<ChecklistCategory, CategoryProgress>
    ) {
        if (weaponCount == 0) return

        try {
            val weapons = getWeaponEntitiesSync()

            // Calculate total badges dynamically from database
            var totalBadges = 0
            var completedBadges = 0

            for (weaponEntity in weapons) {
                val weaponId = weaponEntity.data["id"]?.asInt() ?: continue

                val (completed, total) = kotlinx.coroutines.runBlocking {
                    masteryBadgeRepository.getBadgeProgress(weaponId)
                }

                totalBadges += total
                completedBadges += completed
            }

            progressMap[ChecklistCategory.MASTERY_BADGES] = CategoryProgress(
                category = ChecklistCategory.MASTERY_BADGES,
                totalItems = totalBadges,
                unlockedItems = completedBadges
            )
        } catch (e: Exception) {
            Timber.e(e, "Failed to calculate mastery badge progress")
            // Don't add to progress map if calculation fails
        }
    }

    // =============================================================================================
    // Helper Functions - Data Queries
    // =============================================================================================

    /**
     * Get weapon data as Flow: [id, name, iconUrl, category]
     */
    private fun getWeaponData(): Flow<List<List<Any>>> {
        return realm.query<DynamicEntity>("tableName == $0", ChecklistConstants.Tables.WEAPONS_MP)
            .asFlow()
            .map { results ->
                results.list.mapNotNull { entity ->
                    try {
                        val data = entity.data
                        listOf(
                            data["id"]?.asInt() ?: 0,
                            data["display_name"]?.asString() ?: "",
                            data["icon_url"]?.asString() ?: "",
                            data["category"]?.asString() ?: "" // No hardcoded default - use empty string
                        )
                    } catch (e: Exception) {
                        Timber.w(e, "Failed to parse weapon entity")
                        null
                    }
                }.sortedBy { it[1] as String }
            }
    }

    /**
     * Get weapon entities as Flow
     */
    private fun getWeaponEntities(): Flow<List<DynamicEntity>> {
        return realm.query<DynamicEntity>("tableName == $0", ChecklistConstants.Tables.WEAPONS_MP)
            .asFlow()
            .map { it.list }
    }

    /**
     * Get weapon entities synchronously (for use in combine blocks)
     */
    private fun getWeaponEntitiesSync(): List<DynamicEntity> {
        return realm.query<DynamicEntity>("tableName == $0", ChecklistConstants.Tables.WEAPONS_MP).find()
    }

    /**
     * Get checklist unlock state as Flow
     * Strips the compound key prefix to return original IDs
     */
    private fun getChecklistMap(category: ChecklistCategory): Flow<Map<String, Boolean>> {
        return realm.query<ChecklistItemEntity>("category == $0", category.name)
            .asFlow()
            .map { results ->
                results.list.associate {
                    // Strip compound key prefix: "CATEGORY_ID" -> "ID"
                    val originalId = it.id.removePrefix("${category.name}_")
                    originalId to it.isUnlocked
                }
            }
    }

    /**
     * Build camo query cache by fetching all camo data once
     * Prevents N+1 query problem
     */
    private fun buildCamoQueryCache(): CamoQueryCache {
        try {
            // Query all weapon-camo assignments from junction table ONLY
            // Each weapon gets exactly its assigned camos from weapon_camo table
            val weaponCamoMap = realm.query<DynamicEntity>(
                "tableName == $0",
                ChecklistConstants.Tables.WEAPON_CAMO
            ).find()
                .mapNotNull { entity ->
                    val weaponId = entity.data["weapon_id"]?.asInt()
                    val camoId = entity.data["camo_id"]?.asInt()
                    if (weaponId != null && camoId != null) weaponId to camoId else null
                }
                .groupBy({ it.first }, { it.second })

            // Query all camo criteria
            val criteriaMap = realm.query<DynamicEntity>(
                "tableName == $0",
                ChecklistConstants.Tables.CAMO_CRITERIA
            ).find()
                .mapNotNull { entity ->
                    val weaponId = entity.data["weapon_id"]?.asInt()
                    val camoId = entity.data["camo_id"]?.asInt()
                    val criterionId = entity.data["id"]?.asInt()
                    if (weaponId != null && camoId != null && criterionId != null) {
                        Triple(weaponId, camoId, criterionId)
                    } else null
                }
                .groupBy({ it.first to it.second }, { it.third })

            return CamoQueryCache(weaponCamoMap, criteriaMap)
        } catch (e: Exception) {
            Timber.e(e, "Failed to build camo query cache")
            return CamoQueryCache(emptyMap(), emptyMap())
        }
    }

    // =============================================================================================
    // Helper Functions - Calculations
    // =============================================================================================

    /**
     * Count unlocked camos for a weapon
     */
    private fun countUnlockedCamos(
        weaponId: Int,
        camoIds: Set<Int>,
        camoCache: CamoQueryCache,
        prefs: Preferences
    ): Int {
        return camoIds.count { camoId ->
            val criteriaIds = camoCache.getCriteriaForCamo(weaponId, camoId)
            if (criteriaIds.isNullOrEmpty()) {
                false
            } else {
                // Camo is unlocked if ALL criteria are completed
                criteriaIds.all { criterionId ->
                    val key = booleanPreferencesKey(
                        ChecklistConstants.PreferenceKeys.weaponCamoCriterion(weaponId, camoId, criterionId)
                    )
                    prefs[key] ?: false
                }
            }
        }
    }

    /**
     * Count how many camos in the list are completed for this weapon
     * Uses pre-fetched criteria map for performance
     */
    private fun countCompletedCamos(
        weaponId: Int,
        camoIds: List<Int>,
        criteriaMap: Map<Pair<Int, Int>, List<Int>>,
        prefs: Preferences
    ): Int {
        return camoIds.count { camoId ->
            val criteriaIds = criteriaMap[weaponId to camoId] ?: emptyList()
            if (criteriaIds.isEmpty()) {
                false
            } else {
                // Camo is completed if ALL criteria are completed
                criteriaIds.all { criterionId ->
                    val key = booleanPreferencesKey(
                        ChecklistConstants.PreferenceKeys.weaponCamoCriterion(weaponId, camoId, criterionId)
                    )
                    prefs[key] ?: false
                }
            }
        }
    }
}
