package com.phoenix.companionforcodblackops7.feature.prestige.data.repository

import com.phoenix.companionforcodblackops7.core.data.local.entity.DynamicEntity
import com.phoenix.companionforcodblackops7.feature.checklist.domain.model.ChecklistConstants
import com.phoenix.companionforcodblackops7.feature.prestige.domain.model.PrestigeItem
import com.phoenix.companionforcodblackops7.feature.prestige.domain.model.PrestigeType
import com.phoenix.companionforcodblackops7.feature.prestige.domain.repository.PrestigeRepository
import io.realm.kotlin.Realm
import io.realm.kotlin.ext.query
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Prestige repository implementation
 * Completely dynamic - queries prestige data from database
 * NO HARDCODED prestige counts, milestones, or levels
 *
 * Data source: classic_prestige table
 * Enforces strict sequential unlocking via CategoryChecklistViewModel
 */
@Singleton
class PrestigeRepositoryImpl @Inject constructor(
    private val realm: Realm
) : PrestigeRepository {

    override fun getAllPrestigeItems(): Flow<List<PrestigeItem>> {
        return realm.query<DynamicEntity>("tableName == $0", ChecklistConstants.Tables.CLASSIC_PRESTIGE)
            .asFlow()
            .map { results ->
                results.list.mapNotNull { entity ->
                    try {
                        val data = entity.data

                        // Parse prestige data from database dynamically
                        val id = data["id"]?.asString() ?: return@mapNotNull null
                        val name = data["name"]?.asString() ?: return@mapNotNull null
                        val typeStr = data["type"]?.asString() ?: "PRESTIGE"
                        val level = data["level"]?.asInt() ?: 0
                        val description = data["description"]?.asString() ?: ""
                        val iconPath = data["icon_path"]?.asString() ?: ""

                        val type = when (typeStr.uppercase()) {
                            "PRESTIGE_MASTER" -> PrestigeType.PRESTIGE_MASTER
                            else -> PrestigeType.PRESTIGE
                        }

                        PrestigeItem(
                            id = id,
                            name = name,
                            type = type,
                            level = level,
                            description = description,
                            iconPath = iconPath
                        )
                    } catch (e: Exception) {
                        Timber.w(e, "Failed to parse prestige entity")
                        null
                    }
                }.sortedBy { it.level }
            }
    }
}
