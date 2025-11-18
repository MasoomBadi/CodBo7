package com.phoenix.companionforcodblackops7.feature.combatspecialties.data.repository

import com.phoenix.companionforcodblackops7.core.data.local.entity.DynamicEntity
import com.phoenix.companionforcodblackops7.feature.combatspecialties.domain.model.CombatSpecialty
import com.phoenix.companionforcodblackops7.feature.combatspecialties.domain.repository.CombatSpecialtiesRepository
import io.realm.kotlin.Realm
import io.realm.kotlin.ext.asFlow
import io.realm.kotlin.ext.query
import io.realm.kotlin.types.RealmAny
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import timber.log.Timber
import javax.inject.Inject

class CombatSpecialtiesRepositoryImpl @Inject constructor(
    private val realm: Realm
) : CombatSpecialtiesRepository {

    override fun getAllCombatSpecialties(): Flow<List<CombatSpecialty>> {
        return realm.query<DynamicEntity>("tableName == $0", "combat_specialties")
            .asFlow()
            .map { results ->
                results.list.mapNotNull { entity ->
                    try {
                        deserializeCombatSpecialty(entity)
                    } catch (e: Exception) {
                        Timber.e(e, "Failed to deserialize combat specialty: ${entity.id}")
                        null
                    }
                }.sortedBy { it.sortOrder }
            }
    }

    override fun getCombatSpecialtiesByType(type: String): Flow<List<CombatSpecialty>> {
        return getAllCombatSpecialties().map { specialties ->
            specialties.filter { it.specialtyType.equals(type, ignoreCase = true) }
        }
    }

    override fun getCombatSpecialtyById(id: Int): Flow<CombatSpecialty?> {
        return getAllCombatSpecialties().map { specialties ->
            specialties.find { it.id == id }
        }
    }

    private fun deserializeCombatSpecialty(entity: DynamicEntity): CombatSpecialty {
        val data = entity.data

        fun getString(key: String, default: String = ""): String {
            val value = data[key]
            return when {
                value == null -> default
                value.type == RealmAny.Type.STRING -> value.asString()
                else -> default
            }
        }

        fun getInt(key: String, default: Int = 0): Int {
            val value = data[key]
            return when {
                value == null -> default
                value.type == RealmAny.Type.INT -> value.asInt().toInt()
                value.type == RealmAny.Type.STRING -> value.asString().toIntOrNull() ?: default
                else -> default
            }
        }

        return CombatSpecialty(
            id = getInt("id"),
            name = getString("name", ""),
            displayName = getString("display_name", ""),
            specialtyType = getString("specialty_type", ""),
            categoryColor = getString("category_color", ""),
            requiredPerks = getString("required_perks", ""),
            perkCombination = getString("perk_combination", ""),
            effectDescription = getString("effect_description", ""),
            iconUrl = getString("icon_url", ""),
            sortOrder = getInt("sort_order", 0)
        )
    }
}
