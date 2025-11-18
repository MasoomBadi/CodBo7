package com.phoenix.companionforcodblackops7.feature.scorestreaks.data.repository

import com.phoenix.companionforcodblackops7.core.data.local.entity.DynamicEntity
import com.phoenix.companionforcodblackops7.feature.scorestreaks.domain.model.Scorestreak
import com.phoenix.companionforcodblackops7.feature.scorestreaks.domain.repository.ScorestreaksRepository
import io.realm.kotlin.Realm
import io.realm.kotlin.ext.asFlow
import io.realm.kotlin.ext.query
import io.realm.kotlin.types.RealmAny
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import timber.log.Timber
import javax.inject.Inject

class ScorestreaksRepositoryImpl @Inject constructor(
    private val realm: Realm
) : ScorestreaksRepository {

    override fun getAllScorestreaks(): Flow<List<Scorestreak>> {
        return realm.query<DynamicEntity>("tableName == $0", "scorestreaks")
            .asFlow()
            .map { results ->
                results.list.mapNotNull { entity ->
                    try {
                        deserializeScorestreak(entity)
                    } catch (e: Exception) {
                        Timber.e(e, "Failed to deserialize scorestreak: ${entity.id}")
                        null
                    }
                }.sortedBy { it.sortOrder }
            }
    }

    override fun getScorestreakById(id: Int): Flow<Scorestreak?> {
        return getAllScorestreaks().map { scorestreaks ->
            scorestreaks.find { it.id == id }
        }
    }

    private fun deserializeScorestreak(entity: DynamicEntity): Scorestreak {
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

        return Scorestreak(
            id = getInt("id"),
            name = getString("name", ""),
            displayName = getString("display_name", ""),
            description = getString("description", ""),
            scoreCost = getInt("score_cost", 0),
            unlockLevel = getInt("unlock_level", 0),
            unlockLabel = getString("unlock_label", ""),
            overclock1 = getString("overclock_1", ""),
            overclock2 = getString("overclock_2", ""),
            iconUrl = getString("icon_url", ""),
            iconO1Url = getString("icon_o1_url", ""),
            iconO2Url = getString("icon_o2_url", ""),
            sortOrder = getInt("sort_order", 0)
        )
    }
}
