package com.phoenix.companionforcodblackops7.feature.weaponcamo.data.repository

import com.phoenix.companionforcodblackops7.core.data.local.entity.DynamicEntity
import com.phoenix.companionforcodblackops7.feature.checklist.domain.model.ChecklistConstants
import com.phoenix.companionforcodblackops7.feature.weaponcamo.domain.model.Camo
import com.phoenix.companionforcodblackops7.feature.weaponcamo.domain.model.CamoCategory
import com.phoenix.companionforcodblackops7.feature.weaponcamo.domain.model.CamoCriteria
import com.phoenix.companionforcodblackops7.feature.weaponcamo.domain.model.Weapon
import com.phoenix.companionforcodblackops7.feature.weaponcamo.domain.repository.WeaponCamoRepository
import io.realm.kotlin.Realm
import io.realm.kotlin.ext.query
import io.realm.kotlin.types.RealmAny
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Implementation of WeaponCamoRepository
 *
 * OPTIMIZED VERSION:
 * - Heavy computations run on Dispatchers.Default
 * - Cached static data (modes, camo definitions)
 * - distinctUntilChanged to prevent unnecessary emissions
 */
@Singleton
class WeaponCamoRepositoryImpl @Inject constructor(
    private val realm: Realm
) : WeaponCamoRepository {

    // =============================================================================================
    // Caches for static data
    // =============================================================================================

    // Cache modes - they never change
    private val cachedModes: List<String> by lazy {
        realm.query<DynamicEntity>(
            "tableName == $0",
            ChecklistConstants.Tables.CAMO
        ).find()
            .mapNotNull { it.data["mode"]?.asString() }
            .distinct()
            .sorted()
            .also { Timber.d("Cached modes: $it") }
    }

    // Cache total camos per weapon (static - doesn't change)
    private val cachedTotalCamosPerWeapon: Int by lazy {
        // 16 camos per mode (campaign, multiplayer, zombie) + 6 prestige = 54 total
        var total = 0
        cachedModes.forEach { mode ->
            val camosInMode = if (mode == "prestige") 6 else 16
            total += camosInMode
        }
        Timber.d("Cached total camos per weapon: $total")
        total
    }

    // Cache all criteria grouped by (weaponId, camoId) -> criterionIds
    // This is loaded once and reused
    @Volatile
    private var cachedAllCriteria: Map<Pair<Int, Int>, List<Int>>? = null

    private fun getAllCriteriaMap(): Map<Pair<Int, Int>, List<Int>> {
        cachedAllCriteria?.let { return it }

        val result = realm.query<DynamicEntity>(
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

        cachedAllCriteria = result
        Timber.d("Cached all criteria: ${result.size} weapon-camo pairs")
        return result
    }

    // Cache weapon-camo mappings for prestige camos
    @Volatile
    private var cachedWeaponCamoMappings: Map<Int, Set<Int>>? = null

    private fun getWeaponCamoMappings(): Map<Int, Set<Int>> {
        cachedWeaponCamoMappings?.let { return it }

        val result = realm.query<DynamicEntity>(
            "tableName == $0",
            ChecklistConstants.Tables.WEAPON_CAMO
        ).find()
            .mapNotNull { entity ->
                val weaponId = entity.data["weapon_id"]?.asInt()
                val camoId = entity.data["camo_id"]?.asInt()
                if (weaponId != null && camoId != null) weaponId to camoId else null
            }
            .groupBy({ it.first }, { it.second })
            .mapValues { it.value.toSet() }

        cachedWeaponCamoMappings = result
        Timber.d("Cached weapon-camo mappings: ${result.size} weapons")
        return result
    }

    // =============================================================================================
    // Progress Tracking
    // =============================================================================================

    private fun getProgressKey(weaponId: Int, camoId: Int, criterionId: Int): String {
        return "w${weaponId}_c${camoId}_cr${criterionId}"
    }

    private suspend fun setCompletedInRealm(weaponId: Int, camoId: Int, criterionId: Int, isCompleted: Boolean) {
        val progressKey = getProgressKey(weaponId, camoId, criterionId)

        realm.write {
            val existing = query<DynamicEntity>("id == $0", progressKey).first().find()

            if (existing != null) {
                findLatest(existing)?.apply {
                    data["is_completed"] = RealmAny.create(isCompleted)
                }
            } else {
                copyToRealm(DynamicEntity().apply {
                    id = progressKey
                    tableName = ChecklistConstants.Tables.USER_WEAPON_CAMO_PROGRESS
                    data["progress_key"] = RealmAny.create(progressKey)
                    data["weapon_id"] = RealmAny.create(weaponId)
                    data["camo_id"] = RealmAny.create(camoId)
                    data["criterion_id"] = RealmAny.create(criterionId)
                    data["is_completed"] = RealmAny.create(isCompleted)
                })
            }
        }
    }

    private fun isCompletedInRealm(weaponId: Int, camoId: Int, criterionId: Int): Boolean {
        val progressKey = getProgressKey(weaponId, camoId, criterionId)
        return realm.query<DynamicEntity>("id == $0", progressKey).first().find()
            ?.data?.get("is_completed")?.asBoolean() ?: false
    }

    // =============================================================================================
    // Flows
    // =============================================================================================

    private fun getAllProgressFlow(): Flow<Set<String>> {
        return realm.query<DynamicEntity>(
            "tableName == $0",
            ChecklistConstants.Tables.USER_WEAPON_CAMO_PROGRESS
        ).asFlow()
            .map { results ->
                results.list
                    .filter { it.data["is_completed"]?.asBoolean() == true }
                    .mapNotNull { it.data["progress_key"]?.asString() }
                    .toSet()
            }
            .distinctUntilChanged()
    }

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

    private fun getProgressFlowForWeapon(weaponId: Int): Flow<Set<String>> {
        return realm.query<DynamicEntity>(
            "tableName == $0 AND data['weapon_id'] == $1",
            ChecklistConstants.Tables.USER_WEAPON_CAMO_PROGRESS,
            weaponId
        ).asFlow()
            .map { results ->
                results.list
                    .filter { it.data["is_completed"]?.asBoolean() == true }
                    .mapNotNull { it.data["progress_key"]?.asString() }
                    .toSet()
            }
            .distinctUntilChanged()
    }

    // =============================================================================================
    // Public API
    // =============================================================================================

    override fun getAllWeapons(): Flow<List<Weapon>> {
        return combine(
            getWeaponsFlow(),
            getAllProgressFlow()
        ) { weapons, completedSet ->
            // Pre-fetch all criteria once
            val allCriteriaMap = getAllCriteriaMap()
            val weaponCamoMappings = getWeaponCamoMappings()

            weapons.mapNotNull { weaponEntity ->
                val weaponId = weaponEntity.data["id"]?.asInt() ?: return@mapNotNull null

                // Calculate progress using cached data
                val (completedCamos, totalCamos, completedModes) = calculateWeaponProgressFast(
                    weaponId, completedSet, allCriteriaMap, weaponCamoMappings
                )

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
                    totalModes = cachedModes.size
                )
            }
                .groupBy { it.category }
                .flatMap { (_, weaponsInCategory) ->
                    weaponsInCategory.sortedBy { it.sortOrder }
                }
        }
            .distinctUntilChanged()
            .flowOn(Dispatchers.Default) // Heavy computation off main thread!
    }

    override suspend fun getWeapon(weaponId: Int): Weapon? {
        val weaponEntity = realm.query<DynamicEntity>(
            "tableName == $0 AND data['id'] == $1",
            ChecklistConstants.Tables.WEAPONS_MP,
            weaponId
        ).first().find() ?: return null

        val allCriteriaMap = getAllCriteriaMap()
        val weaponCamoMappings = getWeaponCamoMappings()
        val completedSet = realm.query<DynamicEntity>(
            "tableName == $0 AND data['weapon_id'] == $1 AND data['is_completed'] == $2",
            ChecklistConstants.Tables.USER_WEAPON_CAMO_PROGRESS,
            weaponId,
            true
        ).find().mapNotNull { it.data["progress_key"]?.asString() }.toSet()

        val (completedCamos, totalCamos, completedModes) = calculateWeaponProgressFast(
            weaponId, completedSet, allCriteriaMap, weaponCamoMappings
        )

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
            totalModes = cachedModes.size
        )
    }

    override fun getCamosForWeapon(weaponId: Int, mode: String): Flow<List<CamoCategory>> {
        return combine(
            getCamosFlow(mode),
            getProgressFlowForWeapon(weaponId)
        ) { camoEntities, completedSet ->
            val allCriteriaMap = getAllCriteriaMap()
            val weaponCamoMappings = getWeaponCamoMappings()

            // Parse all camos
            val allCamos = camoEntities.mapNotNull { entity ->
                parseCamoEntity(entity, weaponId, allCriteriaMap, completedSet)
            }

            // Filter prestige camos for this weapon
            val camosForWeapon = if (mode.lowercase() == "prestige") {
                val weaponPrestigeCamos = weaponCamoMappings[weaponId] ?: emptySet()
                allCamos.filter { camo ->
                    weaponPrestigeCamos.contains(camo.id) ||
                            camo.category in listOf("prestigem1", "prestigem2", "prestigem3")
                }
            } else {
                allCamos
            }

            // Group and apply dependency logic
            groupCamosByCategory(camosForWeapon, mode)
        }
            .distinctUntilChanged()
            .flowOn(Dispatchers.Default)
    }

    override suspend fun getCamoCriteria(weaponId: Int, camoId: Int): List<CamoCriteria> {
        val criteriaEntities = realm.query<DynamicEntity>(
            "tableName == $0 AND data['weapon_id'] == $1 AND data['camo_id'] == $2",
            ChecklistConstants.Tables.CAMO_CRITERIA,
            weaponId,
            camoId
        ).find()

        val completedCriteriaIds = realm.query<DynamicEntity>(
            "tableName == $0 AND data['weapon_id'] == $1 AND data['camo_id'] == $2 AND data['is_completed'] == $3",
            ChecklistConstants.Tables.USER_WEAPON_CAMO_PROGRESS,
            weaponId,
            camoId,
            true
        ).find()
            .mapNotNull { it.data["criterion_id"]?.asInt() }
            .toSet()

        return criteriaEntities.mapNotNull { entity ->
            val id = entity.data["id"]?.asInt() ?: return@mapNotNull null
            CamoCriteria(
                id = id,
                weaponId = weaponId,
                camoId = camoId,
                criteriaOrder = entity.data["criteria_order"]?.asInt() ?: 1,
                criteriaText = entity.data["criteria_text"]?.asString() ?: "",
                isCompleted = completedCriteriaIds.contains(id)
            )
        }.sortedBy { it.criteriaOrder }
    }

    override suspend fun toggleCriterion(weaponId: Int, camoId: Int, criterionId: Int) {
        val currentValue = isCompletedInRealm(weaponId, camoId, criterionId)
        setCompletedInRealm(weaponId, camoId, criterionId, !currentValue)
        Timber.d("Toggled: w=$weaponId, c=$camoId, cr=$criterionId -> ${!currentValue}")
    }

    override suspend fun isCamoUnlocked(
        weaponId: Int,
        camo: Camo,
        allCamosInMode: List<Camo>
    ): Boolean {
        if (camo.categoryOrder == 1 && camo.sortOrder == 1) return true

        if (camo.mode.lowercase() == "prestige") {
            if (camo.categoryOrder > 1) {
                return allCamosInMode
                    .filter { it.categoryOrder == camo.categoryOrder - 1 }
                    .all { it.isCompleted }
            }
            return true
        }

        if (camo.categoryOrder > 1) {
            val previousCategoryComplete = allCamosInMode
                .filter { it.categoryOrder == camo.categoryOrder - 1 }
                .all { it.isCompleted }
            if (!previousCategoryComplete) return false
        }

        if (camo.sortOrder > 1) {
            val previousCamoComplete = allCamosInMode
                .firstOrNull { it.category == camo.category && it.sortOrder == camo.sortOrder - 1 }
                ?.isCompleted ?: false
            if (!previousCamoComplete) return false
        }

        return true
    }

    override suspend fun getWeaponProgress(weaponId: Int): Triple<Int, Int, Int> {
        val completedSet = realm.query<DynamicEntity>(
            "tableName == $0 AND data['weapon_id'] == $1 AND data['is_completed'] == $2",
            ChecklistConstants.Tables.USER_WEAPON_CAMO_PROGRESS,
            weaponId,
            true
        ).find().mapNotNull { it.data["progress_key"]?.asString() }.toSet()

        return calculateWeaponProgressFast(
            weaponId, completedSet, getAllCriteriaMap(), getWeaponCamoMappings()
        )
    }

    // =============================================================================================
    // Helper Methods
    // =============================================================================================

    /**
     * FAST progress calculation using cached data - no DB queries!
     */
    private fun calculateWeaponProgressFast(
        weaponId: Int,
        completedSet: Set<String>,
        allCriteriaMap: Map<Pair<Int, Int>, List<Int>>,
        weaponCamoMappings: Map<Int, Set<Int>>
    ): Triple<Int, Int, Int> {
        var totalCompleted = 0
        var totalCamos = 0
        var completedModes = 0

        val weaponPrestigeCamos = weaponCamoMappings[weaponId] ?: emptySet()

        for (mode in cachedModes) {
            val camoEntities = realm.query<DynamicEntity>(
                "tableName == $0 AND data['mode'] == $1",
                ChecklistConstants.Tables.CAMO,
                mode
            ).find()

            val camosForMode = camoEntities.mapNotNull { entity ->
                val camoId = entity.data["id"]?.asInt() ?: return@mapNotNull null
                val category = entity.data["category"]?.asString() ?: ""

                // Filter prestige camos
                if (mode == "prestige") {
                    if (!weaponPrestigeCamos.contains(camoId) &&
                        category !in listOf("prestigem1", "prestigem2", "prestigem3")
                    ) {
                        return@mapNotNull null
                    }
                }

                // Check if completed using cached criteria
                val criteria = allCriteriaMap[weaponId to camoId] ?: emptyList()
                val completedCriteriaCount = criteria.count { criterionId ->
                    completedSet.contains(getProgressKey(weaponId, camoId, criterionId))
                }
                val isCompleted = criteria.isNotEmpty() && completedCriteriaCount == criteria.size

                camoId to isCompleted
            }

            totalCamos += camosForMode.size
            val completedInMode = camosForMode.count { it.second }
            totalCompleted += completedInMode

            if (camosForMode.isNotEmpty() && completedInMode == camosForMode.size) {
                completedModes++
            }
        }

        return Triple(totalCompleted, totalCamos, completedModes)
    }

    private fun parseCamoEntity(
        entity: DynamicEntity,
        weaponId: Int,
        allCriteriaMap: Map<Pair<Int, Int>, List<Int>>,
        completedSet: Set<String>
    ): Camo? {
        val id = entity.data["id"]?.asInt() ?: return null
        val criteria = allCriteriaMap[weaponId to id] ?: emptyList()
        val completedCriteriaCount = criteria.count { criterionId ->
            completedSet.contains(getProgressKey(weaponId, id, criterionId))
        }
        val isCompleted = criteria.isNotEmpty() && completedCriteriaCount == criteria.size

        return Camo(
            id = id,
            name = entity.data["name"]?.asString() ?: "",
            displayName = entity.data["display_name"]?.asString() ?: "",
            category = entity.data["category"]?.asString() ?: "",
            mode = entity.data["mode"]?.asString() ?: "",
            camoUrl = entity.data["camo_url"]?.asString() ?: "",
            sortOrder = entity.data["sort_order"]?.asInt() ?: 0,
            categoryOrder = entity.data["category_order"]?.asInt() ?: 0,
            isCompleted = isCompleted,
            isLocked = true,
            criteriaCount = criteria.size,
            completedCriteriaCount = completedCriteriaCount
        )
    }

    private fun groupCamosByCategory(camos: List<Camo>, mode: String): List<CamoCategory> {
        val sortedCamos = camos.sortedWith(compareBy({ it.categoryOrder }, { it.sortOrder }))

        // Apply dependency logic inline (no suspend needed)
        val camosWithLockState = sortedCamos.map { camo ->
            val isUnlocked = isCamoUnlockedSync(camo, sortedCamos, mode)
            camo.copy(isLocked = !isUnlocked)
        }

        return camosWithLockState
            .groupBy { it.category }
            .map { (categoryName, camosInCategory) ->
                val categoryOrder = camosInCategory.firstOrNull()?.categoryOrder ?: 0
                val isCategoryLocked = if (categoryOrder > 1) {
                    !camosWithLockState
                        .filter { it.categoryOrder == categoryOrder - 1 }
                        .all { it.isCompleted }
                } else false

                CamoCategory(
                    name = categoryName,
                    displayName = formatCategoryDisplayName(categoryName),
                    categoryOrder = categoryOrder,
                    camos = camosInCategory,
                    isLocked = isCategoryLocked
                )
            }
            .sortedBy { it.categoryOrder }
    }

    private fun isCamoUnlockedSync(camo: Camo, allCamosInMode: List<Camo>, mode: String): Boolean {
        if (camo.categoryOrder == 1 && camo.sortOrder == 1) return true

        if (mode.lowercase() == "prestige") {
            if (camo.categoryOrder > 1) {
                return allCamosInMode
                    .filter { it.categoryOrder == camo.categoryOrder - 1 }
                    .all { it.isCompleted }
            }
            return true
        }

        if (camo.categoryOrder > 1) {
            if (!allCamosInMode.filter { it.categoryOrder == camo.categoryOrder - 1 }.all { it.isCompleted }) {
                return false
            }
        }

        if (camo.sortOrder > 1) {
            if (allCamosInMode.firstOrNull { it.category == camo.category && it.sortOrder == camo.sortOrder - 1 }?.isCompleted != true) {
                return false
            }
        }

        return true
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
}
