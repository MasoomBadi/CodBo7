package com.phoenix.companionforcodblackops7.feature.perks.data.repository

import com.phoenix.companionforcodblackops7.core.data.local.entity.DynamicEntity
import com.phoenix.companionforcodblackops7.feature.perks.domain.model.Perk
import com.phoenix.companionforcodblackops7.feature.perks.domain.repository.PerksRepository
import io.realm.kotlin.Realm
import io.realm.kotlin.ext.asFlow
import io.realm.kotlin.ext.query
import io.realm.kotlin.types.RealmAny
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import timber.log.Timber
import javax.inject.Inject

class PerksRepositoryImpl @Inject constructor(
    private val realm: Realm
) : PerksRepository {

    override fun getAllPerks(): Flow<List<Perk>> {
        return realm.query<DynamicEntity>("tableName == $0", "perks")
            .asFlow()
            .map { results ->
                results.list.mapNotNull { entity ->
                    try {
                        deserializePerk(entity)
                    } catch (e: Exception) {
                        Timber.e(e, "Failed to deserialize perk: ${entity.id}")
                        null
                    }
                }.sortedBy { it.sortOrder }
            }
            .distinctUntilChanged()
            .flowOn(Dispatchers.Default)
    }

    override fun getPerksBySlot(slot: Int): Flow<List<Perk>> {
        return getAllPerks().map { perks ->
            perks.filter { it.slot == slot }
        }
    }

    override fun getPerksByCategory(category: String): Flow<List<Perk>> {
        return getAllPerks().map { perks ->
            perks.filter { it.category.equals(category, ignoreCase = true) }
        }
    }

    override fun getPerkById(id: Int): Flow<Perk?> {
        return getAllPerks().map { perks ->
            perks.find { it.id == id }
        }
    }

    private fun deserializePerk(entity: DynamicEntity): Perk {
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

        return Perk(
            id = getInt("id"),
            name = getString("name", ""),
            displayName = getString("display_name", ""),
            slot = getInt("slot", 1),
            category = getString("category", ""),
            categoryColor = getString("category_color", ""),
            unlockLevel = getInt("unlock_level", 0),
            unlockLabel = getString("unlock_label", ""),
            description = getString("description", ""),
            iconUrl = getString("icon_url", ""),
            sortOrder = getInt("sort_order", 0)
        )
    }
}
