package com.phoenix.companionforcodblackops7.feature.weaponcamos.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import com.phoenix.companionforcodblackops7.core.data.local.entity.DynamicEntity
import com.phoenix.companionforcodblackops7.feature.weaponcamos.domain.model.Camo
import com.phoenix.companionforcodblackops7.feature.weaponcamos.domain.model.CamoCategory
import com.phoenix.companionforcodblackops7.feature.weaponcamos.domain.model.CamoCriteria
import com.phoenix.companionforcodblackops7.feature.weaponcamos.domain.model.CamoMode
import com.phoenix.companionforcodblackops7.feature.weaponcamos.domain.model.WeaponCamoProgress
import com.phoenix.companionforcodblackops7.feature.weaponcamos.domain.repository.WeaponCamosRepository
import io.realm.kotlin.Realm
import io.realm.kotlin.ext.query
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WeaponCamosRepositoryImpl @Inject constructor(
    private val realm: Realm,
    private val dataStore: DataStore<Preferences>
) : WeaponCamosRepository {

    override fun getWeaponCamos(weaponId: Int): Flow<Map<CamoMode, List<Camo>>> {
        // Get common camos (shared by all weapons)
        val commonCamosFlow = realm.query<DynamicEntity>(
            "tableName == $0 AND (data['category'] == $1 OR data['category'] == $2 OR data['category'] == $3 OR data['category'] == $4 OR data['category'] == $5 OR data['category'] == $6)",
            "camo", "military", "special", "mastery", "prestigem1", "prestigem2", "prestigem3"
        ).asFlow().map { results ->
            results.list.mapNotNull { entity ->
                try {
                    mapEntityToCamo(entity)
                } catch (e: Exception) {
                    Timber.e(e, "Error mapping common camo entity")
                    null
                }
            }
        }

        // Get unique camos (weapon-specific from junction table)
        val uniqueCamosFlow = realm.query<DynamicEntity>(
            "tableName == $0 AND data['weapon_id'] == $1",
            "weapon_camo", weaponId
        ).asFlow().map { junctionResults ->
            val camoIds = junctionResults.list.mapNotNull { entity ->
                entity.data["camo_id"]?.asInt()
            }

            // Fetch actual camo data for these IDs
            if (camoIds.isEmpty()) {
                emptyList()
            } else {
                realm.query<DynamicEntity>("tableName == $0", "camo")
                    .find()
                    .filter { entity ->
                        val id = entity.data["id"]?.asInt()
                        id != null && camoIds.contains(id)
                    }
                    .mapNotNull { entity ->
                        try {
                            mapEntityToCamo(entity)
                        } catch (e: Exception) {
                            Timber.e(e, "Error mapping unique camo entity")
                            null
                        }
                    }
            }
        }

        // Combine common and unique camos
        return combine(commonCamosFlow, uniqueCamosFlow) { commonCamos, uniqueCamos ->
            val allCamos = (commonCamos + uniqueCamos)
                .sortedWith(compareBy({ it.mode.ordinal }, { it.category.ordinal }, { it.sortOrder }))

            // Group by mode
            allCamos.groupBy { it.mode }
        }
    }

    override fun getWeaponCamoProgress(weaponId: Int, weaponName: String): Flow<WeaponCamoProgress> {
        return combine(
            getWeaponCamos(weaponId),
            dataStore.data
        ) { camosByMode, prefs ->
            val allCamos = camosByMode.values.flatten()

            // Fetch criteria for all camos and check completion status
            val camosWithCriteria = allCamos.map { camo ->
                val criteria = fetchCamoCriteria(camo.id, weaponId, prefs)

                // Camo is unlocked if all criteria are completed
                val isUnlocked = criteria.isNotEmpty() && criteria.all { it.isCompleted }

                camo.copy(
                    criteria = criteria,
                    isUnlocked = isUnlocked
                )
            }

            val unlockedCount = camosWithCriteria.count { it.isUnlocked }

            WeaponCamoProgress(
                weaponId = weaponId,
                weaponName = weaponName,
                camosByMode = camosWithCriteria.groupBy { it.mode },
                totalCamos = camosWithCriteria.size,
                unlockedCount = unlockedCount
            )
        }
    }

    private fun fetchCamoCriteria(camoId: Int, weaponId: Int, prefs: Preferences): List<CamoCriteria> {
        // Fetch criteria from camo_criteria table
        val criteriaEntities = realm.query<DynamicEntity>(
            "tableName == $0 AND data['camo_id'] == $1",
            "camo_criteria", camoId
        ).find()

        return criteriaEntities.mapNotNull { entity ->
            try {
                val data = entity.data
                val criterionId = data["id"]?.asInt() ?: 0
                val criteriaOrder = data["criteria_order"]?.asInt() ?: 1

                // Check if this criterion is completed from DataStore
                val key = booleanPreferencesKey("weapon_${weaponId}_camo_${camoId}_criterion_${criterionId}")
                val isCompleted = prefs[key] ?: false

                // Determine if this criterion is locked (depends on previous criterion)
                val isLocked = if (criteriaOrder > 1) {
                    // Find previous criterion
                    val previousCriterion = criteriaEntities.find { prevEntity ->
                        prevEntity.data["criteria_order"]?.asInt() == (criteriaOrder - 1)
                    }

                    if (previousCriterion != null) {
                        val prevId = previousCriterion.data["id"]?.asInt() ?: 0
                        val prevKey = booleanPreferencesKey("weapon_${weaponId}_camo_${camoId}_criterion_${prevId}")
                        val prevCompleted = prefs[prevKey] ?: false
                        !prevCompleted // Locked if previous is not completed
                    } else {
                        false
                    }
                } else {
                    false // First criterion is never locked
                }

                CamoCriteria(
                    id = criterionId,
                    camoId = camoId,
                    criteriaOrder = criteriaOrder,
                    criteriaText = data["criteria_text"]?.asString() ?: "",
                    isCompleted = isCompleted,
                    isLocked = isLocked
                )
            } catch (e: Exception) {
                Timber.e(e, "Error mapping camo criteria entity")
                null
            }
        }.sortedBy { it.criteriaOrder }
    }

    private fun mapEntityToCamo(entity: DynamicEntity): Camo {
        val data = entity.data
        return Camo(
            id = data["id"]?.asInt() ?: 0,
            name = data["name"]?.asString() ?: "",
            displayName = data["display_name"]?.asString() ?: "",
            category = CamoCategory.fromString(data["category"]?.asString() ?: "military"),
            mode = CamoMode.fromString(data["mode"]?.asString() ?: "multiplayer"),
            camoUrl = data["camo_url"]?.asString() ?: "",
            sortOrder = data["sort_order"]?.asInt() ?: 0
        )
    }
}
