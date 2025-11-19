package com.phoenix.companionforcodblackops7.feature.fieldupgradeszm.data.repository

import com.phoenix.companionforcodblackops7.core.data.local.entity.DynamicEntity
import com.phoenix.companionforcodblackops7.feature.fieldupgradeszm.domain.model.FieldUpgradeZM
import com.phoenix.companionforcodblackops7.feature.fieldupgradeszm.domain.model.FieldUpgradeZMAugment
import com.phoenix.companionforcodblackops7.feature.fieldupgradeszm.domain.repository.FieldUpgradesZMRepository
import io.realm.kotlin.Realm
import io.realm.kotlin.ext.query
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FieldUpgradesZMRepositoryImpl @Inject constructor(
    private val realm: Realm
) : FieldUpgradesZMRepository {

    override fun getFieldUpgrades(): Flow<List<FieldUpgradeZM>> {
        // Get field upgrades
        val fieldUpgradesFlow = realm.query<DynamicEntity>("tableName == $0", "field_upgrades_zm")
            .asFlow()
            .map { results ->
                results.list.map { entity ->
                    FieldUpgradeZM(
                        id = entity.data["id"]?.asInt() ?: 0,
                        name = entity.data["name"]?.asString() ?: "",
                        description = entity.data["description"]?.asString() ?: "",
                        unlockLevel = entity.data["unlock_level"]?.asInt() ?: 0,
                        unlockLabel = entity.data["unlock_label"]?.asString() ?: "",
                        fireMode = entity.data["fire_mode"]?.asString() ?: "",
                        maxAmmo = entity.data["max_ammo"]?.asString() ?: "",
                        iconUrl = entity.data["icon_url"]?.asString() ?: "",
                        gunUrl = entity.data["gun_url"]?.asString() ?: "",
                        flowUrl = entity.data["flow_url"]?.asString() ?: "",
                        sortOrder = entity.data["sort_order"]?.asInt() ?: 0
                    )
                }.sortedBy { it.sortOrder }
            }

        // Get augments
        val augmentsFlow = realm.query<DynamicEntity>("tableName == $0", "field_upgrade_zm_augments")
            .asFlow()
            .map { results ->
                results.list.map { entity ->
                    FieldUpgradeZMAugment(
                        id = entity.data["id"]?.asInt() ?: 0,
                        fieldUpgradeId = entity.data["field_upgrade_id"]?.asInt() ?: 0,
                        name = entity.data["name"]?.asString() ?: "",
                        type = entity.data["type"]?.asString() ?: "",
                        effect = entity.data["effect"]?.asString() ?: "",
                        sortOrder = entity.data["sort_order"]?.asInt() ?: 0
                    )
                }
            }

        // Combine field upgrades with their augments
        return combine(fieldUpgradesFlow, augmentsFlow) { fieldUpgrades, augments ->
            fieldUpgrades.map { fieldUpgrade ->
                val fieldUpgradeAugments = augments.filter { it.fieldUpgradeId == fieldUpgrade.id }
                fieldUpgrade.copy(augments = fieldUpgradeAugments)
            }
        }
    }
}
