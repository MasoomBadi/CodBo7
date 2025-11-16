package com.phoenix.companionforcodblackops7.feature.maps.domain.repository

import com.phoenix.companionforcodblackops7.feature.maps.domain.model.GameMap
import kotlinx.coroutines.flow.Flow

interface MapsRepository {
    fun getAllMaps(): Flow<List<GameMap>>
    fun getMapById(mapId: String): Flow<GameMap?>
}
