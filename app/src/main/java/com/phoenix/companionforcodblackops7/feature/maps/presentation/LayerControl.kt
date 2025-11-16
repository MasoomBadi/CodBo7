package com.phoenix.companionforcodblackops7.feature.maps.presentation

data class LayerControl(
    val id: String,
    val displayName: String,
    val type: LayerControlType,
    val parentId: String? = null,
    val markerCategory: String? = null,
    val layerKey: String? = null
)

enum class LayerControlType {
    MARKER_CATEGORY,
    MAP_LAYER
}
