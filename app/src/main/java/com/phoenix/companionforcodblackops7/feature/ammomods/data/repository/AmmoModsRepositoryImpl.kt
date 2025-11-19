package com.phoenix.companionforcodblackops7.feature.ammomods.data.repository

import com.phoenix.companionforcodblackops7.core.data.local.entity.DynamicEntity
import com.phoenix.companionforcodblackops7.feature.ammomods.domain.model.AmmoMod
import com.phoenix.companionforcodblackops7.feature.ammomods.domain.model.AmmoModAugment
import com.phoenix.companionforcodblackops7.feature.ammomods.domain.repository.AmmoModsRepository
import io.realm.kotlin.Realm
import io.realm.kotlin.ext.asFlow
import io.realm.kotlin.ext.query
import io.realm.kotlin.types.RealmAny
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import timber.log.Timber
import javax.inject.Inject

class AmmoModsRepositoryImpl @Inject constructor(
    private val realm: Realm
) : AmmoModsRepository {

    override fun getAllAmmoMods(): Flow<List<AmmoMod>> {
        val ammoModsFlow = realm.query<DynamicEntity>("tableName == $0", "ammo_mods")
            .asFlow()
            .map { results ->
                results.list.mapNotNull { entity ->
                    try {
                        deserializeAmmoMod(entity)
                    } catch (e: Exception) {
                        Timber.e(e, "Failed to deserialize ammo mod: ${entity.id}")
                        null
                    }
                }.sortedBy { it.sortOrder }
            }

        val augmentsFlow = realm.query<DynamicEntity>("tableName == $0", "ammo_mod_augments")
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

        // Combine ammo mods with their augments
        return combine(ammoModsFlow, augmentsFlow) { ammoMods, augments ->
            ammoMods.map { ammoMod ->
                val modAugments = augments
                    .filter { it.ammoModId == ammoMod.id }
                    .sortedBy { it.sortOrder }
                ammoMod.copy(augments = modAugments)
            }
        }
    }

    override fun getAmmoModById(id: Int): Flow<AmmoMod?> {
        return getAllAmmoMods().map { ammoMods ->
            ammoMods.find { it.id == id }
        }
    }

    private fun deserializeAmmoMod(entity: DynamicEntity): AmmoMod {
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

        return AmmoMod(
            id = getInt("id"),
            name = getString("name", ""),
            description = getString("description", ""),
            unlockLevel = getInt("unlock_level", 0),
            unlockLabel = getString("unlock_label", ""),
            iconUrl = getString("icon_url", ""),
            boxUrl = getString("box_url", ""),
            recipeUrl = getString("recipe_url", ""),
            sortOrder = getInt("sort_order", 0),
            augments = emptyList() // Will be populated in getAllAmmoMods
        )
    }

    private fun deserializeAugment(entity: DynamicEntity): AmmoModAugment {
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

        return AmmoModAugment(
            id = getInt("id"),
            ammoModId = getInt("ammo_mod_id"),
            name = getString("name", ""),
            type = getString("type", ""),
            effect = getString("effect", ""),
            sortOrder = getInt("sort_order", 0)
        )
    }
}
