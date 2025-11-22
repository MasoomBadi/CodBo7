package com.phoenix.companionforcodblackops7.feature.wildcards.data.repository

import com.phoenix.companionforcodblackops7.core.data.local.entity.DynamicEntity
import com.phoenix.companionforcodblackops7.feature.wildcards.domain.model.Wildcard
import com.phoenix.companionforcodblackops7.feature.wildcards.domain.repository.WildcardsRepository
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

class WildcardsRepositoryImpl @Inject constructor(
    private val realm: Realm
) : WildcardsRepository {

    override fun getAllWildcards(): Flow<List<Wildcard>> {
        return realm.query<DynamicEntity>("tableName == $0", "wildcards")
            .asFlow()
            .map { results ->
                results.list.mapNotNull { entity ->
                    try {
                        deserializeWildcard(entity)
                    } catch (e: Exception) {
                        Timber.e(e, "Failed to deserialize wildcard: ${entity.id}")
                        null
                    }
                }.sortedBy { it.sortOrder }
            }
            .distinctUntilChanged()
            .flowOn(Dispatchers.Default)
    }

    override fun getWildcardById(id: Int): Flow<Wildcard?> {
        return getAllWildcards().map { wildcards ->
            wildcards.find { it.id == id }
        }
    }

    private fun deserializeWildcard(entity: DynamicEntity): Wildcard {
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

        return Wildcard(
            id = getInt("id"),
            name = getString("name", ""),
            displayName = getString("display_name", ""),
            unlockLevel = getInt("unlock_level", 0),
            unlockLabel = getString("unlock_label", ""),
            description = getString("description", ""),
            iconUrl = getString("icon_url", ""),
            sortOrder = getInt("sort_order", 0)
        )
    }
}
