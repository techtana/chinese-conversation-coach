package com.mandarincoach.app.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.mandarincoach.app.data.model.CompletionRecord
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

private val Context.progressStore: DataStore<Preferences> by preferencesDataStore(name = "progress_store")

/**
 * Learner progress (scenario completions, earned items, streaks, stats),
 * kept in its own DataStore file separate from user settings. Structured
 * values are stored as JSON strings and aggregated on write so nothing
 * here grows unbounded.
 */
class ProgressRepository(private val context: Context) {

    companion object {
        private val SCENARIO_COMPLETIONS = stringPreferencesKey("scenario_completions")
        private val EARNED_ITEMS = stringPreferencesKey("earned_items")
    }

    private val json = Json { ignoreUnknownKeys = true }

    /** scenarioId → completion record (first completion wins) */
    val scenarioCompletions: Flow<Map<String, CompletionRecord>> = context.progressStore.data.map { prefs ->
        decode(prefs[SCENARIO_COMPLETIONS], emptyMap())
    }

    /** scenarioId → earned item ids */
    val earnedItems: Flow<Map<String, List<String>>> = context.progressStore.data.map { prefs ->
        decode(prefs[EARNED_ITEMS], emptyMap())
    }

    suspend fun recordScenarioCompletion(scenarioId: String, stage: String) {
        context.progressStore.edit { prefs ->
            val current: Map<String, CompletionRecord> = decode(prefs[SCENARIO_COMPLETIONS], emptyMap())
            if (scenarioId !in current) {
                prefs[SCENARIO_COMPLETIONS] = json.encodeToString(
                    current + (scenarioId to CompletionRecord(System.currentTimeMillis(), stage))
                )
            }
        }
    }

    suspend fun addEarnedItem(scenarioId: String, itemId: String) {
        context.progressStore.edit { prefs ->
            val current: Map<String, List<String>> = decode(prefs[EARNED_ITEMS], emptyMap())
            val forScenario = current[scenarioId] ?: emptyList()
            if (itemId !in forScenario) {
                prefs[EARNED_ITEMS] = json.encodeToString(current + (scenarioId to (forScenario + itemId)))
            }
        }
    }

    private inline fun <reified T> decode(raw: String?, default: T): T =
        raw?.let { runCatching { json.decodeFromString<T>(it) }.getOrNull() } ?: default
}
