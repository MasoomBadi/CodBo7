package com.phoenix.companionforcodblackops7.feature.gamemodes.data.repository

import com.phoenix.companionforcodblackops7.feature.gamemodes.domain.model.GameMode
import com.phoenix.companionforcodblackops7.feature.gamemodes.domain.repository.GameModesRepository
import io.realm.kotlin.Realm
import io.realm.kotlin.ext.asFlow
import io.realm.kotlin.ext.query
import io.realm.kotlin.types.RealmAny
import io.realm.kotlin.types.RealmDictionary
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import timber.log.Timber
import javax.inject.Inject

class DynamicEntity : io.realm.kotlin.types.RealmObject {
    var id: String = ""
    var tableName: String = ""
    var data: RealmDictionary<RealmAny?> = realmDictionaryOf()
}

class GameModesRepositoryImpl @Inject constructor(
    private val realm: Realm
) : GameModesRepository {

    override fun getAllGameModes(): Flow<List<GameMode>> {
        return realm.query<DynamicEntity>("tableName == $0", "game_modes")
            .asFlow()
            .map { results ->
                results.list.mapNotNull { entity ->
                    try {
                        deserializeGameMode(entity)
                    } catch (e: Exception) {
                        Timber.e(e, "Failed to deserialize game mode: ${entity.id}")
                        null
                    }
                }
            }
    }

    private fun deserializeGameMode(entity: DynamicEntity): GameMode {
        val data = entity.data

        fun getString(key: String, default: String = ""): String {
            val value = data[key]
            return when {
                value == null -> default
                value.type == RealmAny.Type.STRING -> value.asString()
                else -> default
            }
        }

        fun getBoolean(key: String, default: Boolean = false): Boolean {
            val value = data[key]
            return when {
                value == null -> default
                value.type == RealmAny.Type.BOOL -> value.asBoolean()
                value.type == RealmAny.Type.INT -> value.asInt() != 0
                else -> default
            }
        }

        return GameMode(
            id = getString("id", entity.id),
            name = getString("name", ""),
            displayName = getString("display_name", ""),
            modeType = getString("mode_type", ""),
            matchTime = getString("match_time", ""),
            scoreLimit = getString("score_limit", ""),
            partySize = getString("party_size", ""),
            teamSize = getString("team_size", ""),
            description = getString("description", ""),
            iconUrl = getString("icon_url", ""),
            isNew = getBoolean("is_new", false),
            isFaceOff = getBoolean("is_face_off", false),
            hasScorestreaks = getBoolean("has_scorestreaks", false),
            hasRespawns = getBoolean("has_respawns", false),
            isHardcoreAvailable = getBoolean("is_hardcore_available", false)
        )
    }
}
