package com.mandarincoach.app.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.mandarincoach.app.data.model.ProficiencyLevel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_prefs")

class UserPreferences(private val context: Context) {

    companion object {
        private val API_KEY = stringPreferencesKey("api_key")
        private val PROFICIENCY_LEVEL = stringPreferencesKey("proficiency_level")
        private val SPEECH_SPEED = floatPreferencesKey("speech_speed")
        private val SHOW_ENGLISH = stringPreferencesKey("show_english")
    }

    val apiKey: Flow<String> = context.dataStore.data.map { it[API_KEY] ?: "" }

    val proficiencyLevel: Flow<ProficiencyLevel> = context.dataStore.data.map {
        runCatching { ProficiencyLevel.valueOf(it[PROFICIENCY_LEVEL] ?: "") }
            .getOrDefault(ProficiencyLevel.BEGINNER)
    }

    val speechSpeed: Flow<Float> = context.dataStore.data.map { it[SPEECH_SPEED] ?: 0.85f }

    val showEnglish: Flow<Boolean> = context.dataStore.data.map {
        (it[SHOW_ENGLISH] ?: "true") == "true"
    }

    suspend fun setApiKey(key: String) {
        context.dataStore.edit { it[API_KEY] = key }
    }

    suspend fun setProficiencyLevel(level: ProficiencyLevel) {
        context.dataStore.edit { it[PROFICIENCY_LEVEL] = level.name }
    }

    suspend fun setSpeechSpeed(speed: Float) {
        context.dataStore.edit { it[SPEECH_SPEED] = speed }
    }

    suspend fun setShowEnglish(show: Boolean) {
        context.dataStore.edit { it[SHOW_ENGLISH] = show.toString() }
    }
}
