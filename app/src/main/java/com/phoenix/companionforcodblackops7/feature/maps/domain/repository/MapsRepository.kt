package com.phoenix.companionforcodblackops7.feature.maps.domain.repository

import com.phoenix.companionforcodblackops7.feature.maps.domain.model.GameMap
import com.phoenix.companionforcodblackops7.feature.maps.domain.model.MapLayer
import com.phoenix.companionforcodblackops7.feature.maps.domain.model.MapMarker
import com.phoenix.companionforcodblackops7.feature.maps.domain.model.MapTile
import kotlinx.coroutines.flow.Flow

interface MapsRepository {
    fun getAllMaps(): Flow<List<GameMap>>
    fun getMapById(mapId: String): Flow<GameMap?>
    fun getLayersForMap(mapId: String): Flow<List<MapLayer>>
    fun getMarkersForMap(mapId: String): Flow<List<MapMarker>>
    fun getTilesForMap(mapId: String, zoomLevel: Int? = null): Flow<List<MapTile>>
}
