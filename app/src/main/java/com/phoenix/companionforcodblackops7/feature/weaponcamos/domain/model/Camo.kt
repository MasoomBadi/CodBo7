package com.phoenix.companionforcodblackops7.feature.weaponcamos.domain.model

data class Camo(
    val id: Int,
    val name: String,
    val displayName: String,
    val category: CamoCategory,
    val mode: CamoMode,
    val camoUrl: String,
    val sortOrder: Int,
    val isUnlocked: Boolean = false
)

enum class CamoCategory(val displayName: String) {
    MILITARY("Military"),
    SPECIAL("Special"),
    MASTERY("Mastery"),
    PRESTIGE1("Prestige 1"),
    PRESTIGE2("Prestige 2"),
    PRESTIGE_MASTER("Prestige Master"),
    PRESTIGE_MASTER_1("Prestige Master 1"),
    PRESTIGE_MASTER_2("Prestige Master 2"),
    PRESTIGE_MASTER_3("Prestige Master 3");

    companion object {
        fun fromString(value: String): CamoCategory {
            return when (value.lowercase()) {
                "military" -> MILITARY
                "special" -> SPECIAL
                "mastery" -> MASTERY
                "prestige1" -> PRESTIGE1
                "prestige2" -> PRESTIGE2
                "prestigem" -> PRESTIGE_MASTER
                "prestigem1" -> PRESTIGE_MASTER_1
                "prestigem2" -> PRESTIGE_MASTER_2
                "prestigem3" -> PRESTIGE_MASTER_3
                else -> MILITARY
            }
        }
    }
}

enum class CamoMode(val displayName: String) {
    CAMPAIGN("Campaign"),
    MULTIPLAYER("Multiplayer"),
    ZOMBIE("Zombie"),
    PRESTIGE("Prestige");

    companion object {
        fun fromString(value: String): CamoMode {
            return when (value.lowercase()) {
                "campaign" -> CAMPAIGN
                "multiplayer" -> MULTIPLAYER
                "zombie" -> ZOMBIE
                "prestige" -> PRESTIGE
                else -> MULTIPLAYER
            }
        }
    }
}

data class WeaponCamoProgress(
    val weaponId: Int,
    val weaponName: String,
    val camosByMode: Map<CamoMode, List<Camo>>,
    val totalCamos: Int,
    val unlockedCount: Int
) {
    val percentage: Float
        get() = if (totalCamos > 0) (unlockedCount.toFloat() / totalCamos) * 100 else 0f
}
