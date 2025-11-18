package com.phoenix.companionforcodblackops7.feature.tacticals.data.repository

import com.phoenix.companionforcodblackops7.core.data.local.entity.DynamicEntity
import com.phoenix.companionforcodblackops7.feature.tacticals.domain.model.Tactical
import com.phoenix.companionforcodblackops7.feature.tacticals.domain.repository.TacticalsRepository
import io.realm.kotlin.Realm
import io.realm.kotlin.ext.asFlow
import io.realm.kotlin.ext.query
import io.realm.kotlin.types.RealmAny
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import timber.log.Timber
import javax.inject.Inject

class TacticalsRepositoryImpl @Inject constructor(
    private val realm: Realm
) : TacticalsRepository {

    override fun getAllTacticals(): Flow<List<Tactical>> {
        return realm.query<DynamicEntity>("tableName == $0", "tacticals_mp")
            .asFlow()
            .map { results ->
                results.list.mapNotNull { entity ->
                    try {
                        deserializeTactical(entity)
                    } catch (e: Exception) {
                        Timber.e(e, "Failed to deserialize tactical: ${entity.id}")
                        null
                    }
                }.sortedBy { it.sortOrder }
            }
    }

    override fun getTacticalById(id: Int): Flow<Tactical?> {
        return getAllTacticals().map { tacticals ->
            tacticals.find { it.id == id }
        }
    }

    private fun deserializeTactical(entity: DynamicEntity): Tactical {
        val data = entity.data

        fun getString(key: String, default: String = ""): String {
            val value = data[key]
            return when {
                value == null -> default
                value.type == RealmAny.Type.STRING -> value.asString()
                else -> default
            }
        }

        fun getStringNullable(key: String): String? {
            val value = data[key]
            return when {
                value == null -> null
                value.type == RealmAny.Type.STRING -> {
                    val str = value.asString()
                    if (str.isEmpty()) null else str
                }
                else -> null
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

        fun getBoolean(key: String, default: Boolean = false): Boolean {
            val value = data[key]
            return when {
                value == null -> default
                value.type == RealmAny.Type.BOOL -> value.asBoolean()
                value.type == RealmAny.Type.INT -> value.asInt().toInt() != 0
                value.type == RealmAny.Type.STRING -> value.asString().equals("1", ignoreCase = true) || value.asString().equals("true", ignoreCase = true)
                else -> default
            }
        }

        return Tactical(
            id = getInt("id"),
            name = getString("name", ""),
            displayName = getString("display_name", ""),
            availableMultiplayer = getBoolean("available_multiplayer", false),
            availableZombies = getBoolean("available_zombies", false),
            unlockLevel = getInt("unlock_level", 0),
            unlockLabel = getString("unlock_label", ""),
            description = getString("description", ""),
            overclock1 = getStringNullable("overclock_1"),
            overclock2 = getStringNullable("overclock_2"),
            iconUrl = getString("icon_url", ""),
            iconHudUrl = getStringNullable("icon_hud_url"),
            iconO1Url = getStringNullable("icon_o1_url"),
            iconO2Url = getStringNullable("icon_o2_url"),
            sortOrder = getInt("sort_order", 0)
        )
    }
}
