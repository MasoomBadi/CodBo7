package com.phoenix.companionforcodblackops7.feature.maps.domain.model

data class MapTile(
    val id: String,
    val mapId: String,
    val zoomLevel: Int,
    val tileX: Int,
    val tileY: Int,
    val tileUrl: String
)
