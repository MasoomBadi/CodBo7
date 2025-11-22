package com.phoenix.companionforcodblackops7.feature.gobblegums.data.repository

import com.phoenix.companionforcodblackops7.core.data.local.entity.DynamicEntity
import com.phoenix.companionforcodblackops7.feature.gobblegums.domain.model.GobbleGum
import com.phoenix.companionforcodblackops7.feature.gobblegums.domain.model.GobbleGumPattern
import com.phoenix.companionforcodblackops7.feature.gobblegums.domain.model.GobbleGumRarity
import com.phoenix.companionforcodblackops7.feature.gobblegums.domain.model.GobbleGumTip
import com.phoenix.companionforcodblackops7.feature.gobblegums.domain.model.GumType
import com.phoenix.companionforcodblackops7.feature.gobblegums.domain.repository.GobbleGumsRepository
import io.realm.kotlin.Realm
import io.realm.kotlin.ext.query
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Implementation of GobbleGumsRepository using Realm database
 */
@Singleton
class GobbleGumsRepositoryImpl @Inject constructor(
    private val realm: Realm
) : GobbleGumsRepository {

    override fun getGobbleGums(): Flow<List<GobbleGum>> {
        val gobblegumsFlow = realm.query<DynamicEntity>("tableName == $0", "gobblegums")
            .asFlow()
            .map { results ->
                results.list.mapNotNull { entity ->
                    try {
                        val data = entity.data
                        GobbleGum(
                            id = data["id"]?.asInt() ?: data["id"]?.asString()?.toIntOrNull() ?: 0,
                            name = data["name"]?.asString() ?: "",
                            rarity = GobbleGumRarity.fromString(data["rarity"]?.asString() ?: "Rare"),
                            color = data["color"]?.asString() ?: "",
                            essenceValue = data["essence_value"]?.asInt() ?: data["essence_value"]?.asString()?.toIntOrNull() ?: 0,
                            pattern = GobbleGumPattern.fromString(data["pattern"]?.asString() ?: "Instant"),
                            activationType = data["activation_type"]?.asString() ?: "",
                            zombiesEffect = data["zombies_effect"]?.asString() ?: "",
                            doa4Effect = data["doa4_effect"]?.asString(),
                            duration = data["duration"]?.asString(),
                            shortDescription = data["short_description"]?.asString() ?: "",
                            gumType = GumType.fromString(data["gum_type"]?.asString() ?: "New"),
                            iconUrl = data["icon_url"]?.asString() ?: "",
                            recyclable = (data["recyclable"]?.asInt() ?: data["recyclable"]?.asString()?.toIntOrNull() ?: 1) == 1,
                            synergy = data["synergy"]?.asString(),
                            tags = data["tags"]?.asString(),
                            sortOrder = data["sort_order"]?.asInt() ?: data["sort_order"]?.asString()?.toIntOrNull() ?: 0,
                            tips = emptyList() // Will be filled by combine
                        )
                    } catch (e: Exception) {
                        Timber.e(e, "Error mapping GobbleGum entity")
                        null
                    }
                }
            }

        val tipsFlow = realm.query<DynamicEntity>("tableName == $0", "gobblegum_tips")
            .asFlow()
            .map { results ->
                results.list.mapNotNull { entity ->
                    try {
                        val data = entity.data
                        GobbleGumTip(
                            id = data["id"]?.asInt() ?: data["id"]?.asString()?.toIntOrNull() ?: 0,
                            gobblegumId = data["gobblegum_id"]?.asInt() ?: data["gobblegum_id"]?.asString()?.toIntOrNull() ?: 0,
                            tip = data["tip"]?.asString() ?: "",
                            sortOrder = data["sort_order"]?.asInt() ?: data["sort_order"]?.asString()?.toIntOrNull() ?: 0
                        )
                    } catch (e: Exception) {
                        Timber.e(e, "Error mapping GobbleGumTip entity")
                        null
                    }
                }
            }

        return combine(gobblegumsFlow, tipsFlow) { gobblegums, tips ->
            gobblegums.map { gobblegum ->
                val gobblegumTips = tips
                    .filter { it.gobblegumId == gobblegum.id }
                    .sortedBy { it.sortOrder }
                gobblegum.copy(tips = gobblegumTips)
            }.sortedBy { it.sortOrder }
        }
            .distinctUntilChanged()
            .flowOn(Dispatchers.Default)
    }
}
