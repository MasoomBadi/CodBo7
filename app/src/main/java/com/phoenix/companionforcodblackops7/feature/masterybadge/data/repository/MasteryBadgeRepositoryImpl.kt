package com.phoenix.companionforcodblackops7.feature.masterybadge.data.repository

import com.phoenix.companionforcodblackops7.core.data.local.entity.DynamicEntity
import com.phoenix.companionforcodblackops7.feature.masterybadge.data.local.MasteryBadgeProgressEntity
import com.phoenix.companionforcodblackops7.feature.masterybadge.domain.model.MasteryBadge
import com.phoenix.companionforcodblackops7.feature.masterybadge.domain.repository.MasteryBadgeRepository
import io.realm.kotlin.Realm
import io.realm.kotlin.ext.query
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Implementation of MasteryBadgeRepository
 * Fetches badge requirements from weapon_mastery_badge table
 * Manages local completion state in Realm
 */
@Singleton
class MasteryBadgeRepositoryImpl @Inject constructor(
    private val realm: Realm
) : MasteryBadgeRepository {

    companion object {
        private const val TABLE_WEAPON_MASTERY_BADGE = "weapon_mastery_badge"
    }

    override fun getBadgesForWeapon(weaponId: Int): Flow<List<MasteryBadge>> {
        // Fetch badge requirements from database
        val badgeRequirementsFlow = realm.query<DynamicEntity>(
            "tableName == $0 AND data['weapon_id'] == $1",
            TABLE_WEAPON_MASTERY_BADGE,
            weaponId
        ).asFlow().map { results ->
            results.list.mapNotNull { entity ->
                try {
                    val data = entity.data
                    MasteryBadge(
                        id = data["id"]?.asInt() ?: 0,
                        weaponId = data["weapon_id"]?.asInt() ?: 0,
                        badgeLevel = data["badge_level"]?.asString() ?: "",
                        mode = data["mode"]?.asString() ?: "",
                        killsRequired = data["kills_required"]?.asInt() ?: 0,
                        sortOrder = data["sort_order"]?.asInt() ?: 0,
                        isCompleted = false, // Will be updated from progress
                        isLocked = false // Will be calculated based on hierarchy
                    )
                } catch (e: Exception) {
                    Timber.w(e, "Failed to parse mastery badge entity")
                    null
                }
            }.sortedWith(compareBy({ it.mode }, { it.sortOrder }))
        }

        // Fetch completion progress from Realm
        val progressFlow = realm.query<MasteryBadgeProgressEntity>(
            "weaponId == $0",
            weaponId
        ).asFlow().map { results ->
            results.list.associate {
                "${it.weaponId}_${it.badgeLevel}_${it.mode}" to it.isCompleted
            }
        }

        // Combine requirements with progress and calculate hierarchy locks
        return combine(badgeRequirementsFlow, progressFlow) { badges, progressMap ->
            // Group badges by mode to check hierarchy within each mode
            val badgesByMode = badges.groupBy { it.mode }

            badges.map { badge ->
                val key = "${badge.weaponId}_${badge.badgeLevel}_${badge.mode}"
                val isCompleted = progressMap[key] ?: false

                // Check if this badge is locked based on hierarchy
                // A badge is locked if any previous badge (lower sortOrder) in the same mode is not completed
                val badgesInSameMode = badgesByMode[badge.mode] ?: emptyList()
                val previousBadges = badgesInSameMode.filter { it.sortOrder < badge.sortOrder }
                val isLocked = previousBadges.any { previousBadge ->
                    val previousKey = "${previousBadge.weaponId}_${previousBadge.badgeLevel}_${previousBadge.mode}"
                    progressMap[previousKey] != true
                }

                badge.copy(
                    isCompleted = isCompleted,
                    isLocked = isLocked
                )
            }
        }
    }

    override suspend fun toggleBadgeCompletion(weaponId: Int, badgeLevel: String, mode: String) {
        try {
            realm.write {
                val id = "${weaponId}_${badgeLevel}_${mode}"
                val existing = query<MasteryBadgeProgressEntity>("id == $0", id).first().find()

                if (existing != null) {
                    existing.isCompleted = !existing.isCompleted
                    Timber.d("Toggled badge $id: ${existing.isCompleted}")
                } else {
                    copyToRealm(MasteryBadgeProgressEntity().apply {
                        this.id = id
                        this.weaponId = weaponId
                        this.badgeLevel = badgeLevel
                        this.mode = mode
                        this.isCompleted = true
                    })
                    Timber.d("Created new completed badge: $id")
                }
            }
        } catch (e: Exception) {
            Timber.e(e, "Failed to toggle badge completion: $weaponId/$badgeLevel/$mode")
            throw e
        }
    }

    override suspend fun getBadgeProgress(weaponId: Int): Pair<Int, Int> {
        return try {
            // Get total count from database
            val totalCount = realm.query<DynamicEntity>(
                "tableName == $0 AND data['weapon_id'] == $1",
                TABLE_WEAPON_MASTERY_BADGE,
                weaponId
            ).count().find().toInt()

            // Get completed count from Realm
            val completedCount = realm.query<MasteryBadgeProgressEntity>(
                "weaponId == $0 AND isCompleted == true",
                weaponId
            ).count().find().toInt()

            Pair(completedCount, totalCount)
        } catch (e: Exception) {
            Timber.e(e, "Failed to get badge progress for weapon $weaponId")
            Pair(0, 0)
        }
    }
}
