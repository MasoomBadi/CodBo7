package com.phoenix.companionforcodblackops7.feature.weapons.data.repository

import com.phoenix.companionforcodblackops7.core.data.local.entity.DynamicEntity
import com.phoenix.companionforcodblackops7.feature.weapons.domain.model.Weapon
import com.phoenix.companionforcodblackops7.feature.weapons.domain.model.WeaponCategory
import com.phoenix.companionforcodblackops7.feature.weapons.domain.model.WeaponType
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
                        Weapon(
                            id = data["id"]?.asInt() ?: 0,
                            name = data["name"]?.asString() ?: "",
                            displayName = data["display_name"]?.asString() ?: "",
                            category = WeaponCategory.fromString(
                                data["category"]?.asString() ?: "Assault Rifle"
                            ),
                            weaponType = WeaponType.fromString(
                                data["weapon_type"]?.asString() ?: "Primary"
                            ),
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
}
