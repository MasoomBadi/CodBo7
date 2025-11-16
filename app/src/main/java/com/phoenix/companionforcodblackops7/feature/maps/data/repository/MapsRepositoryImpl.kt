package com.phoenix.companionforcodblackops7.feature.maps.data.repository

import com.phoenix.companionforcodblackops7.core.data.local.entity.DynamicEntity
import com.phoenix.companionforcodblackops7.feature.maps.domain.model.Bounds
import com.phoenix.companionforcodblackops7.feature.maps.domain.model.GameMap
import com.phoenix.companionforcodblackops7.feature.maps.domain.model.MapLayer
import com.phoenix.companionforcodblackops7.feature.maps.domain.model.MapMarker
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

    override fun getLayersForMap(mapId: String): Flow<List<MapLayer>> {
        return realm.query<DynamicEntity>(
            "tableName == $0 AND data['map_id'] == $1",
            "map_layers",
            mapId
        )
            .asFlow()
            .map { results ->
                results.list.mapNotNull { entity ->
                    try {
                        deserializeMapLayer(entity)
                    } catch (e: Exception) {
                        Timber.e(e, "Failed to deserialize layer: ${entity.id}")
                        null
                    }
                }.sortedBy { it.displayOrder }
            }
    }

    override fun getMarkersForMap(mapId: String): Flow<List<MapMarker>> {
        return realm.query<DynamicEntity>(
            "tableName == $0 AND data['map_id'] == $1",
            "map_markers",
            mapId
        )
            .asFlow()
            .map { results ->
                results.list.mapNotNull { entity ->
                    try {
                        deserializeMapMarker(entity)
                    } catch (e: Exception) {
                        Timber.e(e, "Failed to deserialize marker: ${entity.id}")
                        null
                    }
                }
            }
    }

    private fun deserializeMapLayer(entity: DynamicEntity): MapLayer {
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
                value.type == RealmAny.Type.INT -> value.asInt()
                value.type == RealmAny.Type.DOUBLE -> value.asDouble().toInt()
                value.type == RealmAny.Type.FLOAT -> value.asFloat().toInt()
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

        return MapLayer(
            id = getString("id", entity.id),
            mapId = getString("map_id", ""),
            layerName = getString("layer_name", ""),
            layerType = getString("layer_type", ""),
            imageUrl = getString("image_url", ""),
            isDefaultVisible = getBoolean("is_default_visible", true),
            displayOrder = getInt("display_order", 0),
            parentLayerId = getString("parent_layer_id", "").takeIf { it.isNotEmpty() },
            category = getString("category", "")
        )
    }

    private fun deserializeMapMarker(entity: DynamicEntity): MapMarker {
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
                value.type == RealmAny.Type.INT -> value.asInt()
                value.type == RealmAny.Type.DOUBLE -> value.asDouble().toInt()
                value.type == RealmAny.Type.FLOAT -> value.asFloat().toInt()
                else -> default
            }
        }

        return MapMarker(
            id = getString("id", entity.id),
            mapId = getString("map_id", ""),
            layerId = getString("layer_id", ""),
            markerType = getString("marker_type", ""),
            coordX = getInt("coord_x", 0),
            coordY = getInt("coord_y", 0),
            iconUrl = getString("icon_url", ""),
            label = getString("label", ""),
            description = getString("description", "")
        )
    }
}
