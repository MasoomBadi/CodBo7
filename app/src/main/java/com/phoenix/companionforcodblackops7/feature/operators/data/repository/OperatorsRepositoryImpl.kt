package com.phoenix.companionforcodblackops7.feature.operators.data.repository

import com.phoenix.companionforcodblackops7.core.data.local.entity.DynamicEntity
import com.phoenix.companionforcodblackops7.feature.operators.domain.model.Operator
import com.phoenix.companionforcodblackops7.feature.operators.domain.repository.OperatorsRepository
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
class OperatorsRepositoryImpl @Inject constructor(
    private val realm: Realm
) : OperatorsRepository {

    override fun getAllOperators(): Flow<List<Operator>> {
        return realm.query<DynamicEntity>("tableName == $0", "operators")
            .asFlow()
            .map { results ->
                results.list.mapNotNull { entity ->
                    try {
                        deserializeOperator(entity)
                    } catch (e: Exception) {
                        Timber.e(e, "Failed to deserialize operator: ${entity.id}")
                        null
                    }
                }
            }
            .distinctUntilChanged()
            .flowOn(Dispatchers.Default)
    }

    private fun deserializeOperator(entity: DynamicEntity): Operator {
        val data = entity.data

        // Log the actual structure to understand the data
        Timber.d("Operator data keys: ${data.keys}")

        // Helper function to safely get string values
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

        // Helper function to safely get boolean values
        fun getBoolean(key: String, default: Boolean = false): Boolean {
            val value = data[key]
            return when {
                value == null -> default
                value.type == RealmAny.Type.BOOL -> value.asBoolean()
                value.type == RealmAny.Type.INT -> value.asInt() != 0
                value.type == RealmAny.Type.STRING -> {
                    val str = value.asString().lowercase()
                    str == "true" || str == "1"
                }
                else -> default
            }
        }

        return Operator(
            id = getString("id", entity.id),
            shortName = getString("short_name", "Unknown"),
            fullName = getString("full_name", "Unknown Operator"),
            nationality = getString("nationality", "Unknown"),
            division = getString("divison", "Unknown"), // API has typo: "divison" instead of "division"
            zombiePlayable = getBoolean("zombie_playable", false),
            description = getString("description", ""),
            unlockCriteria = getString("unlock_criteria", ""),
            imageUrl = getString("image_url", "")
        )
    }
}
