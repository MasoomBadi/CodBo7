package com.phoenix.companionforcodblackops7.feature.weaponcamo.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import com.phoenix.companionforcodblackops7.core.data.local.entity.DynamicEntity
import com.phoenix.companionforcodblackops7.feature.checklist.domain.model.ChecklistConstants
import com.phoenix.companionforcodblackops7.feature.weaponcamo.domain.model.Camo
import com.phoenix.companionforcodblackops7.feature.weaponcamo.domain.model.CamoCategory
import com.phoenix.companionforcodblackops7.feature.weaponcamo.domain.model.CamoCriteria
import com.phoenix.companionforcodblackops7.feature.weaponcamo.domain.model.Weapon
import com.phoenix.companionforcodblackops7.feature.weaponcamo.domain.repository.WeaponCamoRepository
import io.realm.kotlin.Realm
import io.realm.kotlin.ext.query
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Implementation of WeaponCamoRepository
 *
 * Handles weapon camo tracking with dynamic database structure and complex dependency logic
 */
@Singleton
class WeaponCamoRepositoryImpl @Inject constructor(
    private val realm: Realm,
    private val dataStore: DataStore<Preferences>
) : WeaponCamoRepository {

    // Cache total modes count to ensure consistency across all weapons
    private val cachedTotalModes: Int by lazy {
        val count = getDistinctModes().size
        Timber.d("Cached total modes count: $count")
        count
    }

    // =============================================================================================
    // Weapons
    // =============================================================================================

    override fun getAllWeapons(): Flow<List<Weapon>> {
        return combine(
            getWeaponsFlow(),
            dataStore.data
        ) { weapons, prefs ->
            weapons.map { weaponEntity ->
                val weaponId = weaponEntity.data["id"]?.asInt() ?: return@map null
                val (completedCamos, totalCamos, completedModes) = calculateWeaponProgressSync(weaponId, prefs)

                Weapon(
                    id = weaponId,
                    name = weaponEntity.data["name"]?.asString() ?: "",
                    displayName = weaponEntity.data["display_name"]?.asString() ?: "",
                    category = weaponEntity.data["category"]?.asString() ?: "",
                    weaponType = weaponEntity.data["weapon_type"]?.asString() ?: "",
                    iconUrl = weaponEntity.data["icon_url"]?.asString() ?: "",
                    sortOrder = weaponEntity.data["sort_order"]?.asInt() ?: 0,
                    completedCamos = completedCamos,
                    totalCamos = totalCamos,
                    completedModes = completedModes,
                    totalModes = getTotalModesCount()
                )
            }.filterNotNull()
                .groupBy { it.category }
                .flatMap { (_, weaponsInCategory) ->
                    weaponsInCategory.sortedBy { it.sortOrder }
                }
        }
    }

    override suspend fun getWeapon(weaponId: Int): Weapon? {
        val weaponEntity = realm.query<DynamicEntity>(
            "tableName == $0 AND data['id'] == $1",
            ChecklistConstants.Tables.WEAPONS_MP,
            weaponId
        ).first().find() ?: return null

        val prefs = dataStore.data.first()
        val (completedCamos, totalCamos, completedModes) = calculateWeaponProgressSync(weaponId, prefs)

        return Weapon(
            id = weaponId,
            name = weaponEntity.data["name"]?.asString() ?: "",
            displayName = weaponEntity.data["display_name"]?.asString() ?: "",
            category = weaponEntity.data["category"]?.asString() ?: "",
            weaponType = weaponEntity.data["weapon_type"]?.asString() ?: "",
            iconUrl = weaponEntity.data["icon_url"]?.asString() ?: "",
            sortOrder = weaponEntity.data["sort_order"]?.asInt() ?: 0,
            completedCamos = completedCamos,
            totalCamos = totalCamos,
            completedModes = completedModes,
            totalModes = getTotalModesCount()
        )
    }

    // =============================================================================================
    // Camos
    // =============================================================================================

    override fun getCamosForWeapon(weaponId: Int, mode: String): Flow<List<CamoCategory>> {
        return combine(
            getCamosFlow(mode),
            dataStore.data
        ) { camoEntities, prefs ->
            // Parse camos from database
            val allCamos = camoEntities.mapNotNull { entity ->
                parseCamoEntity(entity, weaponId, prefs)
            }

            // Filter: For prestige mode, include only camos assigned to this weapon
            val camosForWeapon = if (mode.lowercase() == "prestige") {
                filterPrestigeCamosForWeapon(weaponId, allCamos)
            } else {
                allCamos
            }

            // Group by category and apply dependency logic
            groupCamosByCategory(weaponId, camosForWeapon)
        }
    }

    override suspend fun getCamoCriteria(weaponId: Int, camoId: Int): List<CamoCriteria> {
        val criteriaEntities = realm.query<DynamicEntity>(
            "tableName == $0 AND data['weapon_id'] == $1 AND data['camo_id'] == $2",
            ChecklistConstants.Tables.CAMO_CRITERIA,
            weaponId,
            camoId
        ).find()

        val prefs = dataStore.data.first()

        return criteriaEntities.mapNotNull { entity ->
            val id = entity.data["id"]?.asInt() ?: return@mapNotNull null
            val criteriaOrder = entity.data["criteria_order"]?.asInt() ?: 1
            val criteriaText = entity.data["criteria_text"]?.asString() ?: ""

            val key = booleanPreferencesKey(
                ChecklistConstants.PreferenceKeys.weaponCamoCriterion(weaponId, camoId, id)
            )
            val isCompleted = prefs[key] ?: false

            CamoCriteria(
                id = id,
                weaponId = weaponId,
                camoId = camoId,
                criteriaOrder = criteriaOrder,
                criteriaText = criteriaText,
                isCompleted = isCompleted
            )
        }.sortedBy { it.criteriaOrder }
    }

    // =============================================================================================
    // Progress & State Management
    // =============================================================================================

    override suspend fun toggleCriterion(weaponId: Int, camoId: Int, criterionId: Int) {
        val key = booleanPreferencesKey(
            ChecklistConstants.PreferenceKeys.weaponCamoCriterion(weaponId, camoId, criterionId)
        )
        dataStore.edit { prefs ->
            val currentValue = prefs[key] ?: false
            prefs[key] = !currentValue
            Timber.d("Toggled criterion: weapon=$weaponId, camo=$camoId, criterion=$criterionId, newValue=${!currentValue}")
        }
    }

    override suspend fun isCamoUnlocked(
        weaponId: Int,
        camo: Camo,
        allCamosInMode: List<Camo>
    ): Boolean {
        // Prestige mode: All camos are unlocked from the start (no category dependency)
        // Each prestige category is independent
        if (camo.mode.lowercase() == "prestige") {
            return true
        }

        // Rule 1: First camo in first category is always unlocked
        if (camo.categoryOrder == 1 && camo.sortOrder == 1) {
            return true
        }

        // Rule 2: Check category dependency
        // All camos in previous category_order must be complete
        if (camo.categoryOrder > 1) {
            val previousCategoryComplete = allCamosInMode
                .filter { it.categoryOrder == camo.categoryOrder - 1 }
                .all { it.isCompleted }

            if (!previousCategoryComplete) {
                return false
            }
        }

        // Rule 3: Check item dependency within category
        // Previous sort_order in same category must be complete
        if (camo.sortOrder > 1) {
            val previousCamoComplete = allCamosInMode
                .firstOrNull {
                    it.category == camo.category &&
                            it.sortOrder == camo.sortOrder - 1
                }?.isCompleted ?: false

            if (!previousCamoComplete) {
                return false
            }
        }

        return true
    }

    override suspend fun getWeaponProgress(weaponId: Int): Triple<Int, Int, Int> {
        val prefs = dataStore.data.first()
        return calculateWeaponProgressSync(weaponId, prefs)
    }

    // =============================================================================================
    // Helper Methods
    // =============================================================================================

    private fun getWeaponsFlow(): Flow<List<DynamicEntity>> {
        return realm.query<DynamicEntity>(
            "tableName == $0",
            ChecklistConstants.Tables.WEAPONS_MP
        ).asFlow().map { it.list }
    }

    private fun getCamosFlow(mode: String): Flow<List<DynamicEntity>> {
        return realm.query<DynamicEntity>(
            "tableName == $0 AND data['mode'] == $1",
            ChecklistConstants.Tables.CAMO,
            mode.lowercase()
        ).asFlow().map { it.list }
    }

    private fun parseCamoEntity(
        entity: DynamicEntity,
        weaponId: Int,
        prefs: Preferences
    ): Camo? {
        val id = entity.data["id"]?.asInt() ?: return null
        val name = entity.data["name"]?.asString() ?: ""
        val displayName = entity.data["display_name"]?.asString() ?: ""
        val category = entity.data["category"]?.asString() ?: ""
        val mode = entity.data["mode"]?.asString() ?: ""
        val camoUrl = entity.data["camo_url"]?.asString() ?: ""
        val sortOrder = entity.data["sort_order"]?.asInt() ?: 0
        val categoryOrder = entity.data["category_order"]?.asInt() ?: 0

        // Calculate criteria completion
        val criteria = getCriteriaForCamo(weaponId, id)
        val completedCriteriaCount = criteria.count { criterionId ->
            val key = booleanPreferencesKey(
                ChecklistConstants.PreferenceKeys.weaponCamoCriterion(weaponId, id, criterionId)
            )
            prefs[key] ?: false
        }
        val totalCriteriaCount = criteria.size
        val isCompleted = totalCriteriaCount > 0 && completedCriteriaCount == totalCriteriaCount

        return Camo(
            id = id,
            name = name,
            displayName = displayName,
            category = category,
            mode = mode,
            camoUrl = camoUrl,
            sortOrder = sortOrder,
            categoryOrder = categoryOrder,
            isCompleted = isCompleted,
            isLocked = true, // Will be computed later based on dependencies
            criteriaCount = totalCriteriaCount,
            completedCriteriaCount = completedCriteriaCount
        )
    }

    /**
     * Optimized version that uses pre-fetched criteria map
     * Used by calculateWeaponProgressSync to avoid N+1 query problem
     */
    private fun parseCamoEntityOptimized(
        entity: DynamicEntity,
        weaponId: Int,
        prefs: Preferences,
        criteriaMap: Map<Int, List<Int>>
    ): Camo? {
        val id = entity.data["id"]?.asInt() ?: return null
        val name = entity.data["name"]?.asString() ?: ""
        val displayName = entity.data["display_name"]?.asString() ?: ""
        val category = entity.data["category"]?.asString() ?: ""
        val mode = entity.data["mode"]?.asString() ?: ""
        val camoUrl = entity.data["camo_url"]?.asString() ?: ""
        val sortOrder = entity.data["sort_order"]?.asInt() ?: 0
        val categoryOrder = entity.data["category_order"]?.asInt() ?: 0

        // Use pre-fetched criteria map instead of querying database
        val criteria = criteriaMap[id] ?: emptyList()
        val completedCriteriaCount = criteria.count { criterionId ->
            val key = booleanPreferencesKey(
                ChecklistConstants.PreferenceKeys.weaponCamoCriterion(weaponId, id, criterionId)
            )
            prefs[key] ?: false
        }
        val totalCriteriaCount = criteria.size
        val isCompleted = totalCriteriaCount > 0 && completedCriteriaCount == totalCriteriaCount

        return Camo(
            id = id,
            name = name,
            displayName = displayName,
            category = category,
            mode = mode,
            camoUrl = camoUrl,
            sortOrder = sortOrder,
            categoryOrder = categoryOrder,
            isCompleted = isCompleted,
            isLocked = true, // Will be computed later based on dependencies
            criteriaCount = totalCriteriaCount,
            completedCriteriaCount = completedCriteriaCount
        )
    }

    private fun getCriteriaForCamo(weaponId: Int, camoId: Int): List<Int> {
        return realm.query<DynamicEntity>(
            "tableName == $0 AND data['weapon_id'] == $1 AND data['camo_id'] == $2",
            ChecklistConstants.Tables.CAMO_CRITERIA,
            weaponId,
            camoId
        ).find()
            .mapNotNull { it.data["id"]?.asInt() }
    }

    private suspend fun filterPrestigeCamosForWeapon(
        weaponId: Int,
        allPrestigeCamos: List<Camo>
    ): List<Camo> {
        // Get weapon-specific prestige camo mappings from weapon_camo table
        val weaponCamoMappings = realm.query<DynamicEntity>(
            "tableName == $0 AND data['weapon_id'] == $1",
            ChecklistConstants.Tables.WEAPON_CAMO,
            weaponId
        ).find()
            .mapNotNull { it.data["camo_id"]?.asInt() }
            .toSet()

        // Include camos that are either:
        // 1. Mapped to this weapon in weapon_camo table (unique: prestige1, prestige2, prestigel)
        // 2. Common prestige camos (prestigem1, prestigem2, prestigem3)
        return allPrestigeCamos.filter { camo ->
            weaponCamoMappings.contains(camo.id) ||
                    camo.category in listOf("prestigem1", "prestigem2", "prestigem3")
        }
    }

    private suspend fun groupCamosByCategory(
        weaponId: Int,
        camos: List<Camo>
    ): List<CamoCategory> {
        // Sort camos by category_order, then sort_order
        val sortedCamos = camos.sortedWith(
            compareBy<Camo> { it.categoryOrder }.thenBy { it.sortOrder }
        )

        // Apply dependency logic to determine locked state
        val camosWithLockState = sortedCamos.map { camo ->
            val isUnlocked = isCamoUnlocked(weaponId, camo, sortedCamos)
            camo.copy(isLocked = !isUnlocked)
        }

        // Group by category
        val grouped = camosWithLockState.groupBy { it.category }

        return grouped.map { (categoryName, camosInCategory) ->
            val firstCamo = camosInCategory.firstOrNull()
            val categoryOrder = firstCamo?.categoryOrder ?: 0

            // Category is locked if any previous category is incomplete
            val isCategoryLocked = if (categoryOrder > 1) {
                val previousCategoryComplete = camosWithLockState
                    .filter { it.categoryOrder == categoryOrder - 1 }
                    .all { it.isCompleted }
                !previousCategoryComplete
            } else {
                false
            }

            CamoCategory(
                name = categoryName,
                displayName = formatCategoryDisplayName(categoryName),
                categoryOrder = categoryOrder,
                camos = camosInCategory,
                isLocked = isCategoryLocked
            )
        }.sortedBy { it.categoryOrder }
    }

    private fun formatCategoryDisplayName(category: String): String {
        return when (category.lowercase()) {
            "military" -> "Military"
            "special" -> "Special"
            "mastery" -> "Mastery"
            "prestige1" -> "Prestige 1"
            "prestige2" -> "Prestige 2"
            "prestigem1" -> "Prestige Master 1"
            "prestigem2" -> "Prestige Master 2"
            "prestigem3" -> "Prestige Master 3"
            "prestigel" -> "Prestige Legend"
            else -> category.replaceFirstChar { it.uppercase() }
        }
    }

    private fun calculateWeaponProgressSync(weaponId: Int, prefs: Preferences): Triple<Int, Int, Int> {
        try {
            // Query distinct modes from database (dynamic, not hardcoded)
            val modes = getDistinctModes()
            var totalCompleted = 0
            var totalCamos = 0
            var completedModes = 0

            // OPTIMIZATION: Batch-fetch all criteria for this weapon once (1 query instead of 54)
            val allCriteriaMap = realm.query<DynamicEntity>(
                "tableName == $0 AND data['weapon_id'] == $1",
                ChecklistConstants.Tables.CAMO_CRITERIA,
                weaponId
            ).find()
                .mapNotNull { entity ->
                    val camoId = entity.data["camo_id"]?.asInt()
                    val criterionId = entity.data["id"]?.asInt()
                    if (camoId != null && criterionId != null) camoId to criterionId else null
                }
                .groupBy({ it.first }, { it.second })

            for (mode in modes) {
                val camoEntities = realm.query<DynamicEntity>(
                    "tableName == $0 AND data['mode'] == $1",
                    ChecklistConstants.Tables.CAMO,
                    mode
                ).find()

                val camosForMode = camoEntities.mapNotNull { entity ->
                    parseCamoEntityOptimized(entity, weaponId, prefs, allCriteriaMap)
                }

                // Filter prestige camos for this weapon
                val camosForWeapon = if (mode == "prestige") {
                    kotlinx.coroutines.runBlocking {
                        filterPrestigeCamosForWeapon(weaponId, camosForMode)
                    }
                } else {
                    camosForMode
                }

                totalCamos += camosForWeapon.size
                val completedInMode = camosForWeapon.count { it.isCompleted }
                totalCompleted += completedInMode

                // Mode is complete if ALL camos in that mode are unlocked
                if (camosForWeapon.isNotEmpty() && completedInMode == camosForWeapon.size) {
                    completedModes++
                }
            }

            return Triple(totalCompleted, totalCamos, completedModes)
        } catch (e: Exception) {
            Timber.e(e, "Failed to calculate weapon progress for weapon $weaponId")
            return Triple(0, 0, 0) // Default to zeros on error
        }
    }

    /**
     * Get distinct modes from database dynamically
     * @return List of mode names (e.g., ["campaign", "multiplayer", "zombie", "prestige"])
     */
    private fun getDistinctModes(): List<String> {
        val modes = realm.query<DynamicEntity>(
            "tableName == $0",
            ChecklistConstants.Tables.CAMO
        ).find()
            .mapNotNull { it.data["mode"]?.asString() }
            .distinct()
            .sorted()

        Timber.d("Distinct modes from database: $modes (count: ${modes.size})")
        return modes
    }

    /**
     * Get total number of modes from database dynamically
     * Uses cached value for consistency across all weapon instances
     * @return Total count of distinct modes
     */
    private fun getTotalModesCount(): Int {
        Timber.d("Returning cached total modes count: $cachedTotalModes")
        return cachedTotalModes
    }
}
