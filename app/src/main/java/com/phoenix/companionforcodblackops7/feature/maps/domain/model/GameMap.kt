package com.phoenix.companionforcodblackops7.feature.maps.domain.model

data class GameMap(
    val id: String,
    val name: String,
    val displayName: String,
    val type: String,
    val baseImageUrl: String,
    val coverImageUrl: String,
    val teams: String,
    val modes: String,
    val campaignMap: String,
    val location: String,
    val bounds: Bounds
)

data class Bounds(
    val northeastX: Int,
    val northeastY: Int,
    val southwestX: Int,
    val southwestY: Int
)
