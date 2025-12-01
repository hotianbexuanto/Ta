package com.hotian.ta.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

object PreferencesKeys {
    val USE_DYNAMIC_COLOR = booleanPreferencesKey("use_dynamic_color")
    val CUSTOM_PRIMARY_COLOR = longPreferencesKey("custom_primary_color")
    val DEVELOPER_MODE = booleanPreferencesKey("developer_mode")
}

class SettingsRepository(private val context: Context) {
    val useDynamicColor: Flow<Boolean> = context.dataStore.data
        .map { preferences ->
            preferences[PreferencesKeys.USE_DYNAMIC_COLOR] ?: false
        }

    val customPrimaryColor: Flow<Long> = context.dataStore.data
        .map { preferences ->
            preferences[PreferencesKeys.CUSTOM_PRIMARY_COLOR] ?: 0xFFB186D6
        }

    val developerMode: Flow<Boolean> = context.dataStore.data
        .map { preferences ->
            preferences[PreferencesKeys.DEVELOPER_MODE] ?: false
        }

    suspend fun setUseDynamicColor(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.USE_DYNAMIC_COLOR] = enabled
        }
    }

    suspend fun setCustomPrimaryColor(color: Long) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.CUSTOM_PRIMARY_COLOR] = color
        }
    }

    suspend fun setDeveloperMode(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.DEVELOPER_MODE] = enabled
        }
    }
}
