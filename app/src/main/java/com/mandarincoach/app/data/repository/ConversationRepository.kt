package com.mandarincoach.app.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.mandarincoach.app.data.model.Message
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

private val Context.conversationStore: DataStore<Preferences> by preferencesDataStore(name = "conversation_store")

class ConversationRepository(private val context: Context) {

    companion object {
        private val CURRENT_MESSAGES = stringPreferencesKey("current_messages")
        private val ALL_SESSIONS_DATA = stringPreferencesKey("all_sessions_data")
    }

    private val json = Json { ignoreUnknownKeys = true }

    val currentMessages: Flow<List<Message>> = context.conversationStore.data.map { prefs ->
        decode(prefs[CURRENT_MESSAGES], emptyList())
    }

    suspend fun saveMessages(messages: List<Message>) {
        context.conversationStore.edit { prefs ->
            prefs[CURRENT_MESSAGES] = json.encodeToString(messages)
        }
    }

    suspend fun clearCurrentMessages() {
        context.conversationStore.edit { prefs ->
            prefs.remove(CURRENT_MESSAGES)
        }
    }

    /**
     * Appends a summarized session to the history. 
     * In a production app, this should eventually move to a real DB.
     */
    suspend fun archiveSession(sessionId: String, summary: String, learningSummary: String, messages: List<Message>) {
        context.conversationStore.edit { prefs ->
            val history: List<SessionArchive> = decode(prefs[ALL_SESSIONS_DATA], emptyList())
            val newArchive = SessionArchive(
                sessionId = sessionId,
                timestamp = System.currentTimeMillis(),
                summary = summary,
                learningSummary = learningSummary,
                messages = messages
            )
            prefs[ALL_SESSIONS_DATA] = json.encodeToString(history + newArchive)
        }
    }

    val allSessions: Flow<List<SessionArchive>> = context.conversationStore.data.map { prefs ->
        decode(prefs[ALL_SESSIONS_DATA], emptyList())
    }

    private inline fun <reified T> decode(raw: String?, default: T): T =
        raw?.let { runCatching { json.decodeFromString<T>(it) }.getOrNull() } ?: default
}

@kotlinx.serialization.Serializable
data class SessionArchive(
    val sessionId: String,
    val timestamp: Long,
    val summary: String,
    val learningSummary: String,
    val messages: List<Message>
)
