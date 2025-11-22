package com.phoenix.companionforcodblackops7.feature.masterybadge.data.preferences

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MasteryBadgePreferencesManager @Inject constructor(
    private val dataStore: DataStore<Preferences>
) {

    /**
     * Update multiplayer kills for a weapon
     */
    suspend fun updateMpKills(weaponId: Int, kills: Int) {
        val key = intPreferencesKey("weapon_${weaponId}_mp_kills")
        dataStore.edit { prefs ->
            prefs[key] = kills.coerceAtLeast(0) // Ensure non-negative
        }
    }

    /**
     * Update zombie kills for a weapon
     */
    suspend fun updateZmKills(weaponId: Int, kills: Int) {
        val key = intPreferencesKey("weapon_${weaponId}_zm_kills")
        dataStore.edit { prefs ->
            prefs[key] = kills.coerceAtLeast(0) // Ensure non-negative
        }
    }

    /**
     * Increment multiplayer kills for a weapon
     */
    suspend fun incrementMpKills(weaponId: Int, increment: Int = 1) {
        val key = intPreferencesKey("weapon_${weaponId}_mp_kills")
        dataStore.edit { prefs ->
            val currentKills = prefs[key] ?: 0
            prefs[key] = (currentKills + increment).coerceAtLeast(0)
        }
    }

    /**
     * Increment zombie kills for a weapon
     */
    suspend fun incrementZmKills(weaponId: Int, increment: Int = 1) {
        val key = intPreferencesKey("weapon_${weaponId}_zm_kills")
        dataStore.edit { prefs ->
            val currentKills = prefs[key] ?: 0
            prefs[key] = (currentKills + increment).coerceAtLeast(0)
        }
    }

    /**
     * Reset kills for a weapon (both modes)
     */
    suspend fun resetWeaponKills(weaponId: Int) {
        val mpKey = intPreferencesKey("weapon_${weaponId}_mp_kills")
        val zmKey = intPreferencesKey("weapon_${weaponId}_zm_kills")
        dataStore.edit { prefs ->
            prefs[mpKey] = 0
            prefs[zmKey] = 0
        }
    }
}
