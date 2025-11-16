package com.phoenix.companionforcodblackops7.feature.maps.domain.model

data class MapMarker(
    val id: String,
    val mapId: String,
    val layerId: String,
    val markerType: String,
    val coordX: Int,
    val coordY: Int,
    val iconUrl: String,
    val label: String,
    val description: String
)
