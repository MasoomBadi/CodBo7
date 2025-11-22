package com.phoenix.companionforcodblackops7.feature.maps.data.repository

import com.phoenix.companionforcodblackops7.core.data.local.entity.DynamicEntity
import com.phoenix.companionforcodblackops7.feature.maps.domain.model.Bounds
import com.phoenix.companionforcodblackops7.feature.maps.domain.model.GameMap
import com.phoenix.companionforcodblackops7.feature.maps.domain.model.MapLayer
import com.phoenix.companionforcodblackops7.feature.maps.domain.model.MapMarker
import com.phoenix.companionforcodblackops7.feature.maps.domain.model.MapTile
import com.phoenix.companionforcodblackops7.feature.maps.domain.repository.MapsRepository
import io.realm.kotlin.Realm
import io.realm.kotlin.ext.query
import io.realm.kotlin.types.RealmAny
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flowOn
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
            .distinctUntilChanged()
            .flowOn(Dispatchers.Default)
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
            type = getString("type", "core"),
            baseImageUrl = getString("base_image_url", ""),
            coverImageUrl = getString("cover_image_url", ""),
            teams = getString("teams", ""),
            modes = getString("modes", ""),
            campaignMap = getString("campaign_map", ""),
            location = getString("location", ""),
            bounds = parseBounds("bounds")
        )
    }

    override fun getLayersForMap(mapId: String): Flow<List<MapLayer>> {
        Timber.d("Querying layers for mapId: $mapId")

        // Convert string mapId to int for query (database stores map_id as integer)
        val mapIdInt = mapId.toIntOrNull() ?: 0

        // Debug: Check all map_layers entities
        val allLayersCount = realm.query<DynamicEntity>("tableName == $0", "map_layers").find().size
        Timber.d("Total map_layers entities in database: $allLayersCount")

        return realm.query<DynamicEntity>(
            "tableName == $0 AND data['map_id'] == $1",
            "map_layers",
            mapIdInt
        )
            .asFlow()
            .map { results ->
                Timber.d("Query returned ${results.list.size} layers for mapId=$mapId (as int: $mapIdInt)")
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
        Timber.d("Querying markers for mapId: $mapId")

        // Convert string mapId to int for query (database stores map_id as integer)
        val mapIdInt = mapId.toIntOrNull() ?: 0

        // Debug: Check all map_markers entities
        val allMarkersCount = realm.query<DynamicEntity>("tableName == $0", "map_markers").find().size
        Timber.d("Total map_markers entities in database: $allMarkersCount")

        return realm.query<DynamicEntity>(
            "tableName == $0 AND data['map_id'] == $1",
            "map_markers",
            mapIdInt
        )
            .asFlow()
            .map { results ->
                Timber.d("Query returned ${results.list.size} markers for mapId=$mapId (as int: $mapIdInt)")
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
            layerKey = getString("layer_key", ""),
            layerName = getString("layer_name", ""),
            layerType = getString("layer_type", ""),
            imageUrl = getString("image_url", ""),
            isDefaultVisible = getBoolean("default_visible", true),
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
            category = getString("category", ""),
            markerType = getString("marker_type", ""),
            coordX = getInt("coord_x", 0),
            coordY = getInt("coord_y", 0),
            iconUrl = getString("icon_url", ""),
            name = getString("name", ""),
            properties = getString("properties", "")
        )
    }

    override fun getTilesForMap(mapId: String, zoomLevel: Int?): Flow<List<MapTile>> {
        Timber.d("Querying tiles for mapId: $mapId, zoomLevel: $zoomLevel")

        val mapIdInt = mapId.toIntOrNull() ?: 0

        return realm.query<DynamicEntity>(
            if (zoomLevel != null) {
                "tableName == $0 AND data['map_id'] == $1 AND data['zoom_level'] == $2"
            } else {
                "tableName == $0 AND data['map_id'] == $1"
            },
            "map_tiles",
            mapIdInt,
            *(if (zoomLevel != null) arrayOf(zoomLevel) else emptyArray())
        )
            .asFlow()
            .map { results ->
                results.list.mapNotNull { entity ->
                    try {
                        deserializeMapTile(entity)
                    } catch (e: Exception) {
                        Timber.e(e, "Failed to deserialize tile: ${entity.id}")
                        null
                    }
                }
            }
    }

    private fun deserializeMapTile(entity: DynamicEntity): MapTile {
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

        return MapTile(
            id = getString("id", entity.id),
            mapId = getInt("map_id", 0).toString(),
            zoomLevel = getInt("zoom_level", 1),
            tileX = getInt("tile_x", 0),
            tileY = getInt("tile_y", 0),
            tileUrl = getString("tile_url", "")
        )
    }
}
