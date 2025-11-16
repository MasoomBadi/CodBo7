package com.phoenix.companionforcodblackops7.feature.maps.data.repository

import com.phoenix.companionforcodblackops7.core.data.local.entity.DynamicEntity
import com.phoenix.companionforcodblackops7.feature.maps.domain.model.Bounds
import com.phoenix.companionforcodblackops7.feature.maps.domain.model.GameMap
import com.phoenix.companionforcodblackops7.feature.maps.domain.repository.MapsRepository
import io.realm.kotlin.Realm
import io.realm.kotlin.ext.query
import io.realm.kotlin.types.RealmAny
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonPrimitive
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MapsRepositoryImpl @Inject constructor(
    private val realm: Realm
) : MapsRepository {

    private val json = Json { ignoreUnknownKeys = true }

    override fun getAllMaps(): Flow<List<GameMap>> {
        return realm.query<DynamicEntity>("tableName == $0", "maps")
            .asFlow()
            .map { results ->
                results.list.mapNotNull { entity ->
                    try {
                        deserializeGameMap(entity)
                    } catch (e: Exception) {
                        Timber.e(e, "Failed to deserialize map: ${entity.id}")
                        null
                    }
                }
            }
    }

    override fun getMapById(mapId: String): Flow<GameMap?> {
        return realm.query<DynamicEntity>(
            "tableName == $0 AND data['id'] == $1",
            "maps",
            mapId
        )
            .asFlow()
            .map { results ->
                results.list.firstOrNull()?.let { entity ->
                    try {
                        deserializeGameMap(entity)
                    } catch (e: Exception) {
                        Timber.e(e, "Failed to deserialize map: ${entity.id}")
                        null
                    }
                }
            }
    }

    private fun deserializeGameMap(entity: DynamicEntity): GameMap {
        val data = entity.data

        // Helper function to safely get string values
        fun getString(key: String, default: String = ""): String {
            val value = data[key]
            return when {
                value == null -> default
                value.type == RealmAny.Type.STRING -> value.asString()
                value.type == RealmAny.Type.INT -> value.asInt().toString()
                value.type == RealmAny.Type.DOUBLE -> value.asDouble().toString()
                value.type == RealmAny.Type.FLOAT -> value.asFloat().toString()
                value.type == RealmAny.Type.BOOL -> value.asBoolean().toString()
                else -> default
            }
        }

        // Parse bounds from JSON string
        fun parseBounds(key: String): Bounds {
            val boundsValue = data[key]
            return try {
                if (boundsValue != null && boundsValue.type == RealmAny.Type.STRING) {
                    val boundsJson = json.parseToJsonElement(boundsValue.asString()) as JsonObject
                    val northeast = boundsJson["northeast"]?.jsonArray
                    val southwest = boundsJson["southwest"]?.jsonArray

                    Bounds(
                        northeastX = northeast?.get(0)?.jsonPrimitive?.content?.toIntOrNull() ?: 2048,
                        northeastY = northeast?.get(1)?.jsonPrimitive?.content?.toIntOrNull() ?: 2048,
                        southwestX = southwest?.get(0)?.jsonPrimitive?.content?.toIntOrNull() ?: 0,
                        southwestY = southwest?.get(1)?.jsonPrimitive?.content?.toIntOrNull() ?: 0
                    )
                } else {
                    // Default bounds
                    Bounds(
                        northeastX = 2048,
                        northeastY = 2048,
                        southwestX = 0,
                        southwestY = 0
                    )
                }
            } catch (e: Exception) {
                Timber.e(e, "Failed to parse bounds, using defaults")
                Bounds(
                    northeastX = 2048,
                    northeastY = 2048,
                    southwestX = 0,
                    southwestY = 0
                )
            }
        }

        return GameMap(
            id = getString("id", entity.id),
            name = getString("name", "Unknown"),
            displayName = getString("display_name", "Unknown Map"),
            baseImageUrl = getString("base_image_url", ""),
            coverImageUrl = getString("cover_image_url", ""),
            teams = getString("teams", ""),
            modes = getString("modes", ""),
            campaignMap = getString("campaign_map", ""),
            bounds = parseBounds("bounds")
        )
    }
}
