package com.example.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "smart_file_settings")

class SettingsRepository(private val context: Context) {

    companion object {
        val GEMINI_API_KEY = stringPreferencesKey("gemini_api_key")
        val HIGH_THINKING_MODE = booleanPreferencesKey("high_thinking_mode")
    }

    val apiKeyFlow: Flow<String?> = context.dataStore.data.map { preferences ->
        preferences[GEMINI_API_KEY]
    }

    val highThinkingModeFlow: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[HIGH_THINKING_MODE] ?: false
    }

    suspend fun saveApiKey(apiKey: String) {
        context.dataStore.edit { preferences ->
            preferences[GEMINI_API_KEY] = apiKey
        }
    }

    suspend fun clearApiKey() {
        context.dataStore.edit { preferences ->
            preferences.remove(GEMINI_API_KEY)
        }
    }

    suspend fun setHighThinkingMode(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[HIGH_THINKING_MODE] = enabled
        }
    }
}
