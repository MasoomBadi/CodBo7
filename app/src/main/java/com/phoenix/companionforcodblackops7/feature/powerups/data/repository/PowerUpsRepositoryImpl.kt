package com.phoenix.companionforcodblackops7.feature.powerups.data.repository

import com.phoenix.companionforcodblackops7.core.data.local.model.DynamicEntity
import com.phoenix.companionforcodblackops7.feature.powerups.domain.model.PowerUp
import com.phoenix.companionforcodblackops7.feature.powerups.domain.repository.PowerUpsRepository
import io.realm.kotlin.Realm
import io.realm.kotlin.ext.query
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Implementation of PowerUpsRepository using Realm database
 */
@Singleton
class PowerUpsRepositoryImpl @Inject constructor(
    private val realm: Realm
) : PowerUpsRepository {

    override fun getPowerUps(): Flow<List<PowerUp>> {
        return realm.query<DynamicEntity>("tableName == $0", "power_ups")
            .asFlow()
            .map { results ->
                results.list.mapNotNull { entity ->
                    try {
                        val data = entity.data
                        PowerUp(
                            id = data["id"]?.asInt() ?: 0,
                            name = data["name"]?.asString() ?: "",
                            description = data["description"]?.asString() ?: "",
                            effectType = data["effect_type"]?.asString() ?: "Instant",
                            duration = data["duration"]?.asString(),
                            iconUrl = data["icon_url"]?.asString() ?: "",
                            sortOrder = data["sort_order"]?.asInt() ?: 0
                        )
                    } catch (e: Exception) {
                        Timber.e(e, "Error mapping PowerUp entity")
                        null
                    }
                }.sortedBy { it.sortOrder }
            }
    }
}
