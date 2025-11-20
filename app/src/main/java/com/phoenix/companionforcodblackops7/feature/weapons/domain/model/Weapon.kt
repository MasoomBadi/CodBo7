package com.phoenix.companionforcodblackops7.feature.weapons.domain.model

data class Weapon(
    val id: Int,
    val name: String,
    val displayName: String,
    val category: WeaponCategory,
    val weaponType: WeaponType,
    val unlockCriteria: String,
    val unlockLevel: Int,
    val unlockLabel: String,
    val maxLevel: Int,
    val fireModes: String,
    val iconUrl: String,
    val sortOrder: Int
)

enum class WeaponCategory(val displayName: String) {
    ASSAULT_RIFLE("Assault Rifles"),
    SMG("SMGs"),
    SHOTGUN("Shotguns"),
    LMG("LMGs"),
    MARKSMAN("Marksman Rifles"),
    SNIPER("Sniper Rifles"),
    PISTOL("Pistols"),
    LAUNCHER("Launchers"),
    MELEE("Melee");

    companion object {
        fun fromString(value: String): WeaponCategory {
            return when (value.uppercase().replace(" ", "_")) {
                "ASSAULT_RIFLE", "ASSAULT RIFLE" -> ASSAULT_RIFLE
                "SMG", "SMGS" -> SMG
                "SHOTGUN", "SHOTGUNS" -> SHOTGUN
                "LMG", "LMGS" -> LMG
                "MARKSMAN", "MARKSMAN_RIFLE", "MARKSMAN RIFLE" -> MARKSMAN
                "SNIPER", "SNIPER_RIFLE", "SNIPER RIFLE" -> SNIPER
                "PISTOL", "PISTOLS" -> PISTOL
                "LAUNCHER", "LAUNCHERS" -> LAUNCHER
                "MELEE" -> MELEE
                else -> ASSAULT_RIFLE
            }
        }
    }
}

enum class WeaponType(val displayName: String) {
    PRIMARY("Primary"),
    SECONDARY("Secondary");

    companion object {
        fun fromString(value: String): WeaponType {
            return when (value.uppercase()) {
                "PRIMARY" -> PRIMARY
                "SECONDARY" -> SECONDARY
                else -> PRIMARY
            }
        }
    }
}
