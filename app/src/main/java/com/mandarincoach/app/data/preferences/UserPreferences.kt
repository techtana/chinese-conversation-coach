package com.mandarincoach.app.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.mandarincoach.app.data.model.LLMProvider
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
        
        // New Profile & Personalization Keys
        private val USER_NAME = stringPreferencesKey("user_name")
        private val LEARNING_GOALS = stringPreferencesKey("learning_goals")
        private val INTERESTS = stringPreferencesKey("interests")
        private val THEME_MODE = stringPreferencesKey("theme_mode") // "system", "light", "dark"
        
        // LLM Backend Keys
        private val LLM_PROVIDER = stringPreferencesKey("llm_provider")
        private val CUSTOM_MODEL = stringPreferencesKey("custom_model")
        private val CUSTOM_BASE_URL = stringPreferencesKey("custom_base_url")
        private val TOTAL_COST = floatPreferencesKey("total_cost")
        
        // Vocabulary Progress Keys
        private val LEARNED_WORDS = stringPreferencesKey("learned_words")
        private val NEW_WORDS_TARGET = intPreferencesKey("new_words_target")
    }

    val apiKey: Flow<String> = context.dataStore.data.map { it[API_KEY] ?: "" }
    val userName: Flow<String> = context.dataStore.data.map { it[USER_NAME] ?: "" }
    val learningGoals: Flow<String> = context.dataStore.data.map { it[LEARNING_GOALS] ?: "" }
    val interests: Flow<String> = context.dataStore.data.map { it[INTERESTS] ?: "" }
    val themeMode: Flow<String> = context.dataStore.data.map { it[THEME_MODE] ?: "system" }

    val llmProvider: Flow<LLMProvider> = context.dataStore.data.map {
        runCatching { LLMProvider.valueOf(it[LLM_PROVIDER] ?: "CLAUDE") }
            .getOrDefault(LLMProvider.CLAUDE)
    }
    
    val customModel: Flow<String> = context.dataStore.data.map { it[CUSTOM_MODEL] ?: "" }
    val customBaseUrl: Flow<String> = context.dataStore.data.map { it[CUSTOM_BASE_URL] ?: "" }
    val totalCost: Flow<Float> = context.dataStore.data.map { it[TOTAL_COST] ?: 0.0f }

    val learnedWords: Flow<Set<String>> = context.dataStore.data.map { 
        it[LEARNED_WORDS]?.split(",")?.filter { s -> s.isNotBlank() }?.toSet() ?: emptySet() 
    }
    
    val newWordsTarget: Flow<Int> = context.dataStore.data.map { it[NEW_WORDS_TARGET] ?: 30 }

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

    suspend fun setUserName(name: String) {
        context.dataStore.edit { it[USER_NAME] = name }
    }

    suspend fun setLearningGoals(goals: String) {
        context.dataStore.edit { it[LEARNING_GOALS] = goals }
    }

    suspend fun setInterests(interests: String) {
        context.dataStore.edit { it[INTERESTS] = interests }
    }

    suspend fun setThemeMode(mode: String) {
        context.dataStore.edit { it[THEME_MODE] = mode }
    }

    suspend fun setLlmProvider(provider: LLMProvider) {
        context.dataStore.edit { it[LLM_PROVIDER] = provider.name }
    }

    suspend fun setCustomModel(model: String) {
        context.dataStore.edit { it[CUSTOM_MODEL] = model }
    }

    suspend fun setCustomBaseUrl(url: String) {
        context.dataStore.edit { it[CUSTOM_BASE_URL] = url }
    }

    suspend fun addCost(amount: Double) {
        context.dataStore.edit { 
            val current = it[TOTAL_COST] ?: 0.0f
            it[TOTAL_COST] = (current + amount).toFloat()
        }
    }

    suspend fun resetCost() {
        context.dataStore.edit { it[TOTAL_COST] = 0.0f }
    }

    suspend fun addLearnedWords(words: Set<String>) {
        context.dataStore.edit { prefs ->
            val current = prefs[LEARNED_WORDS]?.split(",")?.filter { it.isNotBlank() }?.toSet() ?: emptySet()
            val updated = current + words
            prefs[LEARNED_WORDS] = updated.joinToString(",")
        }
    }
    
    suspend fun setNewWordsTarget(target: Int) {
        context.dataStore.edit { it[NEW_WORDS_TARGET] = target }
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
