package com.phoenix.companionforcodblackops7.core.data.local.preferences

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PreferencesManager @Inject constructor(
    private val dataStore: DataStore<Preferences>
) {
    private object PreferencesKeys {
        val IS_SYNC_COMPLETE = booleanPreferencesKey("is_sync_complete")
    }

    val isSyncComplete: Flow<Boolean> = dataStore.data
        .map { preferences ->
            preferences[PreferencesKeys.IS_SYNC_COMPLETE] ?: false
        }

    suspend fun setIsSyncComplete(isComplete: Boolean) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.IS_SYNC_COMPLETE] = isComplete
        }
    }
}
