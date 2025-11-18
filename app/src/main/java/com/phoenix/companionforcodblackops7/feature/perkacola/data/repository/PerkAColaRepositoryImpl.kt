package com.phoenix.companionforcodblackops7.feature.perkacola.data.repository

import com.phoenix.companionforcodblackops7.core.data.local.entity.DynamicEntity
import com.phoenix.companionforcodblackops7.feature.perkacola.domain.model.PerkACola
import com.phoenix.companionforcodblackops7.feature.perkacola.domain.model.PerkAColaAugment
import com.phoenix.companionforcodblackops7.feature.perkacola.domain.repository.PerkAColaRepository
import io.realm.kotlin.Realm
import io.realm.kotlin.ext.asFlow
import io.realm.kotlin.ext.query
import io.realm.kotlin.types.RealmAny
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import timber.log.Timber
import javax.inject.Inject

class PerkAColaRepositoryImpl @Inject constructor(
    private val realm: Realm
) : PerkAColaRepository {

    override fun getAllPerkAColas(): Flow<List<PerkACola>> {
        val perksFlow = realm.query<DynamicEntity>("tableName == $0", "perk_a_cola")
            .asFlow()
            .map { results ->
                results.list.mapNotNull { entity ->
                    try {
                        deserializePerkACola(entity)
                    } catch (e: Exception) {
                        Timber.e(e, "Failed to deserialize perk-a-cola: ${entity.id}")
                        null
                    }
                }.sortedBy { it.sortOrder }
            }

        val augmentsFlow = realm.query<DynamicEntity>("tableName == $0", "perk_a_cola_augments")
            .asFlow()
            .map { results ->
                results.list.mapNotNull { entity ->
                    try {
                        deserializeAugment(entity)
                    } catch (e: Exception) {
                        Timber.e(e, "Failed to deserialize augment: ${entity.id}")
                        null
                    }
                }
            }

        // Combine perks with their augments
        return combine(perksFlow, augmentsFlow) { perks, augments ->
            perks.map { perk ->
                val perkAugments = augments
                    .filter { it.perkId == perk.id }
                    .sortedBy { it.sortOrder }
                perk.copy(augments = perkAugments)
            }
        }
    }

    override fun getPerkAColaById(id: Int): Flow<PerkACola?> {
        return getAllPerkAColas().map { perks ->
            perks.find { it.id == id }
        }
    }

    private fun deserializePerkACola(entity: DynamicEntity): PerkACola {
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

        return PerkACola(
            id = getInt("id"),
            name = getString("name", ""),
            displayName = getString("display_name", ""),
            location = getString("location", ""),
            effect = getString("effect", ""),
            iconUrl = getString("icon_url", ""),
            bottleUrl = getString("bottle_url", ""),
            recipeUrl = getString("recipe_url", ""),
            sortOrder = getInt("sort_order", 0),
            augments = emptyList() // Will be populated in getAllPerkAColas
        )
    }

    private fun deserializeAugment(entity: DynamicEntity): PerkAColaAugment {
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

        return PerkAColaAugment(
            id = getInt("id"),
            perkId = getInt("perk_id"),
            name = getString("name", ""),
            type = getString("type", ""),
            effect = getString("effect", ""),
            sortOrder = getInt("sort_order", 0)
        )
    }
}
