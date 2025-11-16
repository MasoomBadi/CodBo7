package com.phoenix.companionforcodblackops7.feature.maps.domain.model

data class MapLayer(
    val id: String,
    val mapId: String,
    val layerName: String,
    val layerType: String,
    val imageUrl: String,
    val isDefaultVisible: Boolean,
    val displayOrder: Int,
    val parentLayerId: String? = null,
    val category: String = ""
)
