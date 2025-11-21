package com.phoenix.companionforcodblackops7.feature.weaponcamos.data.preferences

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WeaponCamoPreferencesManager @Inject constructor(
    private val dataStore: DataStore<Preferences>
) {

    suspend fun toggleWeaponCamoUnlock(weaponId: Int, camoId: Int) {
        val key = booleanPreferencesKey("weapon_camo_${weaponId}_$camoId")
        dataStore.edit { prefs ->
            val currentValue = prefs[key] ?: false
            prefs[key] = !currentValue
        }
    }

    suspend fun setWeaponCamoUnlocked(weaponId: Int, camoId: Int, unlocked: Boolean) {
        val key = booleanPreferencesKey("weapon_camo_${weaponId}_$camoId")
        dataStore.edit { prefs ->
            prefs[key] = unlocked
        }
    }

    /**
     * Toggle the completion status of a specific criterion for a camo
     */
    suspend fun toggleCriterionCompletion(weaponId: Int, camoId: Int, criterionId: Int) {
        val key = booleanPreferencesKey("weapon_${weaponId}_camo_${camoId}_criterion_${criterionId}")
        dataStore.edit { prefs ->
            val currentValue = prefs[key] ?: false
            prefs[key] = !currentValue
        }
    }

    /**
     * Set the completion status of a specific criterion for a camo
     */
    suspend fun setCriterionCompleted(weaponId: Int, camoId: Int, criterionId: Int, completed: Boolean) {
        val key = booleanPreferencesKey("weapon_${weaponId}_camo_${camoId}_criterion_${criterionId}")
        dataStore.edit { prefs ->
            prefs[key] = completed
        }
    }
}
