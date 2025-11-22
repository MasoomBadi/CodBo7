package com.phoenix.companionforcodblackops7.feature.masterybadge.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.intPreferencesKey
import com.phoenix.companionforcodblackops7.core.data.local.entity.DynamicEntity
import com.phoenix.companionforcodblackops7.feature.masterybadge.domain.model.BadgeProgress
import com.phoenix.companionforcodblackops7.feature.masterybadge.domain.model.MasteryBadge
import com.phoenix.companionforcodblackops7.feature.masterybadge.domain.model.WeaponMasteryProgress
import com.phoenix.companionforcodblackops7.feature.masterybadge.domain.repository.MasteryBadgeRepository
import io.realm.kotlin.Realm
import io.realm.kotlin.ext.query
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MasteryBadgeRepositoryImpl @Inject constructor(
    private val realm: Realm,
    private val dataStore: DataStore<Preferences>
) : MasteryBadgeRepository {

    override fun getWeaponMasteryProgress(
        weaponId: Int,
        weaponName: String,
        weaponCategory: String
    ): Flow<WeaponMasteryProgress> {
        // Fetch mastery badges for this weapon
        val badgesFlow = realm.query<DynamicEntity>(
            "tableName == $0 AND data['weapon_id'] == $1",
            "weapon_mastery_badge", weaponId
        ).asFlow().map { results ->
            results.list.mapNotNull { entity ->
                try {
                    mapEntityToMasteryBadge(entity)
                } catch (e: Exception) {
                    Timber.e(e, "Error mapping mastery badge entity")
                    null
                }
            }.sortedBy { it.sortOrder }
        }

        return combine(badgesFlow, dataStore.data) { badges, prefs ->
            // Get current kills from DataStore
            val mpKillsKey = intPreferencesKey("weapon_${weaponId}_mp_kills")
            val zmKillsKey = intPreferencesKey("weapon_${weaponId}_zm_kills")
            val mpKills = prefs[mpKillsKey] ?: 0
            val zmKills = prefs[zmKillsKey] ?: 0

            // Create badge progress for each mode (using dynamic strings instead of enums)
            val mpBadges = createBadgeProgressList(badges, "mp", "Multiplayer", mpKills)
            val zmBadges = createBadgeProgressList(badges, "zm", "Zombie", zmKills)

            WeaponMasteryProgress(
                weaponId = weaponId,
                weaponName = weaponName,
                weaponCategory = weaponCategory,
                mpKills = mpKills,
                zmKills = zmKills,
                mpBadges = mpBadges,
                zmBadges = zmBadges
            )
        }
    }

    override fun getAllWeaponsMasteryProgress(): Flow<List<WeaponMasteryProgress>> {
        // Fetch all weapons from weapons_mp table
        val weaponsFlow = realm.query<DynamicEntity>("tableName == $0", "weapons_mp")
            .asFlow()
            .map { results ->
                results.list.mapNotNull { entity ->
                    try {
                        val data = entity.data
                        Triple(
                            data["id"]?.asInt() ?: 0,
                            data["display_name"]?.asString() ?: "",
                            data["category"]?.asString() ?: "" // No hardcoded default - use empty string
                        )
                    } catch (e: Exception) {
                        Timber.e(e, "Error mapping weapon entity")
                        null
                    }
                }.sortedBy { it.second } // Sort by weapon name
            }

        return combine(weaponsFlow, dataStore.data) { weapons, _ ->
            weapons.map { (weaponId, weaponName, weaponCategory) ->
                // For each weapon, get its mastery progress
                // Note: This is a simplified version. In production, you'd want to optimize this
                getWeaponMasteryProgressSync(weaponId, weaponName, weaponCategory)
            }
        }
    }

    private fun createBadgeProgressList(
        badges: List<MasteryBadge>,
        mode: String,
        modeDisplayName: String,
        currentKills: Int
    ): List<BadgeProgress> {
        val sortedBadges = badges.sortedBy { it.sortOrder }
        var previousUnlocked = true // First badge is always available

        return sortedBadges.map { badge ->
            val requiredKills = when (mode.lowercase()) {
                "mp", "multiplayer" -> badge.mpKillsRequired
                "zm", "zombie" -> badge.zmKillsRequired
                else -> badge.mpKillsRequired
            }

            // Badge is unlocked if:
            // 1. Kill requirement is met
            // 2. All previous badges are unlocked (sequential dependency)
            val requirementMet = currentKills >= requiredKills
            val isUnlocked = requirementMet && previousUnlocked

            // Current badge is locked if previous badge is not unlocked
            val isLocked = !previousUnlocked

            // Update previousUnlocked for next iteration
            if (!isUnlocked) {
                previousUnlocked = false
            }

            BadgeProgress(
                badge = badge,
                mode = mode,
                modeDisplayName = modeDisplayName,
                currentKills = currentKills,
                requiredKills = requiredKills,
                isUnlocked = isUnlocked,
                isLocked = isLocked
            )
        }
    }

    private fun getWeaponMasteryProgressSync(
        weaponId: Int,
        weaponName: String,
        weaponCategory: String
    ): WeaponMasteryProgress {
        // Fetch badges synchronously for this weapon
        val badges = realm.query<DynamicEntity>(
            "tableName == $0 AND data['weapon_id'] == $1",
            "weapon_mastery_badge", weaponId
        ).find().mapNotNull { entity ->
            try {
                mapEntityToMasteryBadge(entity)
            } catch (e: Exception) {
                Timber.e(e, "Error mapping mastery badge entity")
                null
            }
        }.sortedBy { it.sortOrder }

        // Get current kills from DataStore - this is a sync operation, so we return 0 for now
        // In a real scenario, you'd need to refactor this to use Flow properly
        val mpKills = 0
        val zmKills = 0

        val mpBadges = createBadgeProgressList(badges, "mp", "Multiplayer", mpKills)
        val zmBadges = createBadgeProgressList(badges, "zm", "Zombie", zmKills)

        return WeaponMasteryProgress(
            weaponId = weaponId,
            weaponName = weaponName,
            weaponCategory = weaponCategory,
            mpKills = mpKills,
            zmKills = zmKills,
            mpBadges = mpBadges,
            zmBadges = zmBadges
        )
    }

    /**
     * Map database entity to MasteryBadge model
     * Completely dynamic - fetches all fields from database
     */
    private fun mapEntityToMasteryBadge(entity: DynamicEntity): MasteryBadge {
        val data = entity.data
        val badgeLevel = data["badge_level"]?.asString() ?: ""

        // Generate display name from badge_level if not in database
        val displayName = data["display_name"]?.asString()
            ?: formatBadgeLevelDisplayName(badgeLevel)

        return MasteryBadge(
            id = data["id"]?.asInt() ?: 0,
            weaponId = data["weapon_id"]?.asInt() ?: 0,
            badgeLevel = badgeLevel,
            badgeLevelDisplayName = displayName,
            sortOrder = data["sort_order"]?.asInt() ?: 0,
            mpKillsRequired = data["mp_kills_required"]?.asInt() ?: 0,
            zmKillsRequired = data["zm_kills_required"]?.asInt() ?: 0
        )
    }

    /**
     * Format badge level string to display name
     * Fallback if display_name not in database
     */
    private fun formatBadgeLevelDisplayName(badgeLevel: String): String {
        return when (badgeLevel.lowercase()) {
            "badge_1" -> "Badge I"
            "badge_2" -> "Badge II"
            "badge_3" -> "Badge III"
            "mastery" -> "Mastery"
            else -> badgeLevel.replace("_", " ").capitalize()
        }
    }
}
