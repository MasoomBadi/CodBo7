package com.phoenix.companionforcodblackops7.feature.checklist.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import com.phoenix.companionforcodblackops7.core.data.local.entity.DynamicEntity
import com.phoenix.companionforcodblackops7.feature.checklist.data.local.ChecklistItemEntity
import com.phoenix.companionforcodblackops7.feature.checklist.data.model.CamoQueryCache
import com.phoenix.companionforcodblackops7.feature.checklist.data.model.WeaponMasteryBadgeRequirements
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
    private val dataStore: DataStore<Preferences>
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
            realm.write {
                val existing = query<ChecklistItemEntity>("id == $0", itemId).first().find()

                if (existing != null) {
                    existing.isUnlocked = !existing.isUnlocked
                    Timber.d("Toggled item $itemId: ${existing.isUnlocked}")
                } else {
                    copyToRealm(ChecklistItemEntity().apply {
                        id = itemId
                        this.category = category.name
                        isUnlocked = true
                    })
                    Timber.d("Created new unlocked item: $itemId")
                }
            }
        } catch (e: Exception) {
            Timber.e(e, "Failed to toggle item unlock: $itemId")
            throw e
        }
    }

    override suspend fun isItemUnlocked(itemId: String, category: ChecklistCategory): Boolean {
        return try {
            realm.query<ChecklistItemEntity>("id == $0", itemId)
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
            prestigeItems.map { item ->
                ChecklistItem(
                    id = item.id,
                    name = item.name,
                    category = ChecklistCategory.PRESTIGE,
                    isUnlocked = checklistMap[item.id] ?: false,
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

        return combine(weaponsFlow, dataStore.data) { weapons, prefs ->
            // Fetch all mastery badge data from database once
            val (badgeRequirementsMap, badgeCountsMap) = getAllMasteryBadgeData()

            weapons.map { weaponData ->
                val weaponId = weaponData[0] as Int
                val weaponName = weaponData[1] as String
                val iconUrl = weaponData[2] as String
                val weaponCategory = weaponData[3] as String

                val mpKills = getKillCount(prefs, weaponId, isMpMode = true)
                val zmKills = getKillCount(prefs, weaponId, isMpMode = false)

                // Static: 6 badges per weapon (3 MP + 3 Zombie)
                val totalBadges = 6
                val requirements = badgeRequirementsMap[weaponId]
                val unlockedCount = requirements?.countUnlockedBadges(mpKills, zmKills) ?: 0

                ChecklistItem(
                    id = "$weaponId|$weaponCategory",
                    name = weaponName,
                    category = ChecklistCategory.MASTERY_BADGES,
                    isUnlocked = false, // Not applicable for badges
                    imageUrl = iconUrl,
                    unlockCriteria = "$unlockedCount/$totalBadges badges unlocked"
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
            .filter { it.category == ChecklistCategory.OPERATORS.name }
            .associate { it.id to it.isUnlocked }

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
            .filter { it.category == ChecklistCategory.PRESTIGE.name }
            .associate { it.id to it.isUnlocked }

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
            val camoCache = buildCamoQueryCache()

            var totalCamosCount = 0
            var unlockedCamosCount = 0

            for (weaponEntity in weapons) {
                val weaponId = weaponEntity.data["id"]?.asInt() ?: continue

                val allCamoIds = camoCache.getCamoIdsForWeapon(weaponId)
                totalCamosCount += allCamoIds.size

                val unlockedCount = countUnlockedCamos(weaponId, allCamoIds, camoCache, prefs)
                unlockedCamosCount += unlockedCount
            }

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

            // Fetch all mastery badge data from database once
            val (badgeRequirementsMap, badgeCountsMap) = getAllMasteryBadgeData()

            // Calculate total badges dynamically from database
            var totalBadges = 0
            var unlockedBadges = 0

            for (weaponEntity in weapons) {
                val weaponId = weaponEntity.data["id"]?.asInt() ?: continue

                val mpKills = getKillCount(prefs, weaponId, isMpMode = true)
                val zmKills = getKillCount(prefs, weaponId, isMpMode = false)

                // Static: 6 badges per weapon (3 MP + 3 Zombie)
                val totalBadgesPerWeapon = 6
                totalBadges += totalBadgesPerWeapon

                val requirements = badgeRequirementsMap[weaponId]
                val weaponUnlockedCount = requirements?.countUnlockedBadges(mpKills, zmKills) ?: 0
                unlockedBadges += weaponUnlockedCount
            }

            progressMap[ChecklistCategory.MASTERY_BADGES] = CategoryProgress(
                category = ChecklistCategory.MASTERY_BADGES,
                totalItems = totalBadges,
                unlockedItems = unlockedBadges
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
     */
    private fun getChecklistMap(category: ChecklistCategory): Flow<Map<String, Boolean>> {
        return realm.query<ChecklistItemEntity>("category == $0", category.name)
            .asFlow()
            .map { results ->
                results.list.associate { it.id to it.isUnlocked }
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

    /**
     * Fetch all mastery badge data from database
     * Returns pair of: (requirements map, badge counts map)
     * Completely dynamic - NO HARDCODED badge levels, counts, or strings
     */
    private fun getAllMasteryBadgeData(): Pair<Map<Int, WeaponMasteryBadgeRequirements>, Map<Int, Int>> {
        try {
            val badgeEntities = realm.query<DynamicEntity>(
                "tableName == 'weapon_mastery_badge'"
            ).find()

            // Group by weapon_id
            val grouped = badgeEntities.groupBy { entity ->
                entity.data["weapon_id"]?.asInt() ?: 0
            }

            val requirementsMap = mutableMapOf<Int, WeaponMasteryBadgeRequirements>()
            val countsMap = mutableMapOf<Int, Int>()

            for ((weaponId, entities) in grouped) {
                if (weaponId == 0) continue

                // Count total badges for this weapon dynamically
                countsMap[weaponId] = entities.size

                // Parse all badge requirements dynamically - supports ANY badge levels
                // Sort by sort_order to ensure correct sequential unlocking
                val badgeLevels = entities
                    .map { entity ->
                        val badgeLevel = entity.data["badge_level"]?.asString() ?: ""
                        val mpKillsRequired = entity.data["mp_kills_required"]?.asInt() ?: 0
                        val zmKillsRequired = entity.data["zm_kills_required"]?.asInt() ?: 0
                        val sortOrder = entity.data["sort_order"]?.asInt() ?: 0

                        Pair(sortOrder, Triple(badgeLevel, mpKillsRequired, zmKillsRequired))
                    }
                    .sortedBy { it.first } // Sort by sort_order
                    .map { it.second } // Extract the badge data

                requirementsMap[weaponId] = WeaponMasteryBadgeRequirements(
                    weaponId = weaponId,
                    badgeLevels = badgeLevels
                )
            }

            return Pair(requirementsMap, countsMap)
        } catch (e: Exception) {
            Timber.e(e, "Failed to fetch mastery badge data")
            return Pair(emptyMap(), emptyMap())
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
     * Get kill count from DataStore preferences
     */
    private fun getKillCount(prefs: Preferences, weaponId: Int, isMpMode: Boolean): Int {
        val key = if (isMpMode) {
            ChecklistConstants.PreferenceKeys.weaponMpKills(weaponId)
        } else {
            ChecklistConstants.PreferenceKeys.weaponZmKills(weaponId)
        }
        return prefs[intPreferencesKey(key)] ?: 0
    }
}
