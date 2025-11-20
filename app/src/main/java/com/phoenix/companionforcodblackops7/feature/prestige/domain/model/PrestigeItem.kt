package com.phoenix.companionforcodblackops7.feature.prestige.domain.model

data class PrestigeItem(
    val id: String,
    val name: String,
    val type: PrestigeType,
    val level: Int,
    val description: String
)

enum class PrestigeType(val displayName: String) {
    MILITARY("Military Rank"),
    PRESTIGE("Prestige"),
    PRESTIGE_MASTER("Prestige Master")
}
