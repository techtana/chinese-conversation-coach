package com.mandarincoach.app.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.mandarincoach.app.data.model.CompletionRecord
import com.mandarincoach.app.data.model.StreakState
import com.mandarincoach.app.data.model.WeekAggregate
import com.mandarincoach.app.domain.LatencyStats
import com.mandarincoach.app.domain.StreakManager
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.time.LocalDate

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
        private val STREAK_STATE = stringPreferencesKey("streak_state")
        private val LATENCY_WEEKLY = stringPreferencesKey("latency_weekly")
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

    val streakState: Flow<StreakState> = context.progressStore.data.map { prefs ->
        decode(prefs[STREAK_STATE], StreakState())
    }

    /** weekKey ("2026-W24") → reply-latency aggregate */
    val latencyWeekly: Flow<Map<String, WeekAggregate>> = context.progressStore.data.map { prefs ->
        decode(prefs[LATENCY_WEEKLY], emptyMap())
    }

    /**
     * Marks today as an active day; returns a witty nudge when a streak
     * was just broken, null otherwise.
     */
    suspend fun updateStreak(today: LocalDate = LocalDate.now()): String? {
        var nudge: String? = null
        context.progressStore.edit { prefs ->
            val current: StreakState = decode(prefs[STREAK_STATE], StreakState())
            val result = StreakManager.onAppOpen(today, current)
            nudge = result.nudge
            prefs[STREAK_STATE] = json.encodeToString(result.state)
        }
        return nudge
    }

    suspend fun addLatencySample(deltaMs: Long, today: LocalDate = LocalDate.now()) {
        context.progressStore.edit { prefs ->
            val current: Map<String, WeekAggregate> = decode(prefs[LATENCY_WEEKLY], emptyMap())
            LatencyStats.addSample(current, today, deltaMs)?.let { updated ->
                prefs[LATENCY_WEEKLY] = json.encodeToString(updated)
            }
        }
    }

    private inline fun <reified T> decode(raw: String?, default: T): T =
        raw?.let { runCatching { json.decodeFromString<T>(it) }.getOrNull() } ?: default
}
