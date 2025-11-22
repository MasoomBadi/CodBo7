package com.phoenix.companionforcodblackops7.feature.weapons.data.repository

import com.phoenix.companionforcodblackops7.core.data.local.entity.DynamicEntity
import com.phoenix.companionforcodblackops7.feature.weapons.domain.model.Weapon
import com.phoenix.companionforcodblackops7.feature.weapons.domain.repository.WeaponsRepository
import io.realm.kotlin.Realm
import io.realm.kotlin.ext.query
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WeaponsRepositoryImpl @Inject constructor(
    private val realm: Realm
) : WeaponsRepository {

    override fun getAllWeapons(): Flow<List<Weapon>> {
        return realm.query<DynamicEntity>("tableName == $0", "weapons_mp")
            .asFlow()
            .map { results ->
                results.list.mapNotNull { entity ->
                    try {
                        val data = entity.data
                        val category = data["category"]?.asString() ?: ""
                        val weaponType = data["weapon_type"]?.asString() ?: ""

                        Weapon(
                            id = data["id"]?.asInt() ?: 0,
                            name = data["name"]?.asString() ?: "",
                            displayName = data["display_name"]?.asString() ?: "",
                            category = category,
                            categoryDisplayName = formatCategoryDisplayName(category),
                            weaponType = weaponType,
                            weaponTypeDisplayName = formatWeaponTypeDisplayName(weaponType),
                            unlockCriteria = data["unlock_criteria"]?.asString() ?: "",
                            unlockLevel = data["unlock_level"]?.asInt()
                                ?: data["unlock_level"]?.asString()?.toIntOrNull()
                                ?: 0,
                            unlockLabel = data["unlock_label"]?.asString() ?: "",
                            maxLevel = data["max_level"]?.asInt()
                                ?: data["max_level"]?.asString()?.toIntOrNull()
                                ?: 0,
                            fireModes = data["fire_modes"]?.asString() ?: "",
                            iconUrl = data["icon_url"]?.asString() ?: "",
                            sortOrder = data["sort_order"]?.asInt()
                                ?: data["sort_order"]?.asString()?.toIntOrNull()
                                ?: 0
                        )
                    } catch (e: Exception) {
                        Timber.e(e, "Error mapping Weapon entity")
                        null
                    }
                }.sortedBy { it.sortOrder }
            }
    }

    /**
     * Format category string to display name
     * Fallback if display names not in database
     */
    private fun formatCategoryDisplayName(category: String): String {
        return when (category.uppercase().replace(" ", "_")) {
            "ASSAULT_RIFLE", "ASSAULT RIFLE", "ASSAULT_RIFLES" -> "Assault Rifles"
            "SMG", "SMGS" -> "SMGs"
            "SHOTGUN", "SHOTGUNS" -> "Shotguns"
            "LMG", "LMGS" -> "LMGs"
            "MARKSMAN", "MARKSMAN_RIFLE", "MARKSMAN RIFLE", "MARKSMAN_RIFLES" -> "Marksman Rifles"
            "SNIPER", "SNIPER_RIFLE", "SNIPER RIFLE", "SNIPER_RIFLES" -> "Sniper Rifles"
            "PISTOL", "PISTOLS" -> "Pistols"
            "LAUNCHER", "LAUNCHERS" -> "Launchers"
            "MELEE" -> "Melee"
            else -> category.replace("_", " ").capitalize()
        }
    }

    /**
     * Format weapon type string to display name
     * Fallback if display names not in database
     */
    private fun formatWeaponTypeDisplayName(weaponType: String): String {
        return when (weaponType.uppercase()) {
            "PRIMARY" -> "Primary"
            "SECONDARY" -> "Secondary"
            else -> weaponType.capitalize()
        }
    }
}
