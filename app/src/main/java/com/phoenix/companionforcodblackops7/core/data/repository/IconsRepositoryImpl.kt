package com.phoenix.companionforcodblackops7.core.data.repository

import com.phoenix.companionforcodblackops7.core.data.local.entity.DynamicEntity
import com.phoenix.companionforcodblackops7.core.domain.model.Icon
import com.phoenix.companionforcodblackops7.core.domain.repository.IconsRepository
import io.realm.kotlin.Realm
import io.realm.kotlin.ext.query
import io.realm.kotlin.types.RealmAny
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class IconsRepositoryImpl @Inject constructor(
    private val realm: Realm
) : IconsRepository {

    override fun getIconsByCategory(category: String): Flow<List<Icon>> {
        return realm.query<DynamicEntity>(
            "tableName == $0 AND data['category'] == $1",
            "icons",
            category
        )
            .asFlow()
            .map { results ->
                results.list.mapNotNull { entity ->
                    try {
                        deserializeIcon(entity)
                    } catch (e: Exception) {
                        Timber.e(e, "Failed to deserialize icon: ${entity.id}")
                        null
                    }
                }
            }
            .distinctUntilChanged()
            .flowOn(Dispatchers.Default)
    }

    override fun getIconByName(category: String, name: String): Flow<Icon?> {
        return realm.query<DynamicEntity>(
            "tableName == $0 AND data['category'] == $1 AND data['name'] == $2",
            "icons",
            category,
            name
        )
            .asFlow()
            .map { results ->
                results.list.firstOrNull()?.let { entity ->
                    try {
                        deserializeIcon(entity)
                    } catch (e: Exception) {
                        Timber.e(e, "Failed to deserialize icon: ${entity.id}")
                        null
                    }
                }
            }
    }

    private fun deserializeIcon(entity: DynamicEntity): Icon {
        val data = entity.data

        fun getString(key: String, default: String = ""): String {
            val value = data[key]
            return when {
                value == null -> default
                value.type == RealmAny.Type.STRING -> value.asString()
                value.type == RealmAny.Type.INT -> value.asInt().toString()
                value.type == RealmAny.Type.DOUBLE -> value.asDouble().toString()
                value.type == RealmAny.Type.FLOAT -> value.asFloat().toString()
                value.type == RealmAny.Type.BOOL -> value.asBoolean().toString()
                else -> default
            }
        }

        return Icon(
            id = getString("id", entity.id),
            category = getString("category", ""),
            name = getString("name", ""),
            iconUrl = getString("icon_url", "")
        )
    }
}
