package com.phoenix.companionforcodblackops7.feature.weaponcamo.domain.model

data class Weapon(
    val id: Int,
    val name: String,
    val displayName: String,
    val category: String,
    val weaponType: String,
    val iconUrl: String,
    val sortOrder: Int,
    val completedCamos: Int = 0,
    val totalCamos: Int = 54,
    val completedModes: Int = 0,
    val totalModes: Int = 4  // campaign, multiplayer, zombie, prestige
)
