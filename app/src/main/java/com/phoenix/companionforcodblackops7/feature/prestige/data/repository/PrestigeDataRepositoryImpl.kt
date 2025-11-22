package com.phoenix.companionforcodblackops7.feature.prestige.data.repository

import com.phoenix.companionforcodblackops7.core.data.local.entity.DynamicEntity
import com.phoenix.companionforcodblackops7.feature.prestige.domain.model.PrestigeData
import com.phoenix.companionforcodblackops7.feature.prestige.domain.repository.PrestigeDataRepository
import io.realm.kotlin.Realm
import io.realm.kotlin.ext.query
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Implementation of PrestigeDataRepository using Realm database
 * Fetches prestige data from classic_prestige table
 */
@Singleton
class PrestigeDataRepositoryImpl @Inject constructor(
    private val realm: Realm
) : PrestigeDataRepository {

    override fun getPrestigeData(): Flow<List<PrestigeData>> {
        return realm.query<DynamicEntity>("tableName == $0", "classic_prestige")
            .asFlow()
            .map { results ->
                results.list.mapNotNull { entity ->
                    try {
                        val data = entity.data
                        PrestigeData(
                            id = data["id"]?.asInt() ?: data["id"]?.asString()?.toIntOrNull() ?: 0,
                            title = data["title"]?.asString() ?: "",
                            unlockBy = data["unlock_by"]?.asString() ?: "",
                            icon = data["icon"]?.asString() ?: ""
                        )
                    } catch (e: Exception) {
                        Timber.e(e, "Error mapping PrestigeData entity")
                        null
                    }
                }
            }
            .distinctUntilChanged()
            .flowOn(Dispatchers.Default)
    }
}
