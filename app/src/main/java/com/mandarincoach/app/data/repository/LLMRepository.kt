package com.mandarincoach.app.data.repository

import com.mandarincoach.app.data.model.*
import com.mandarincoach.app.data.preferences.UserPreferences
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.util.concurrent.TimeUnit

class LLMRepository(private val prefs: UserPreferences) {

    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .build()

    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
    }

    companion object {
        private const val API_URL = "https://api.anthropic.com/v1/messages"
        private const val MODEL = "claude-3-haiku-20240307"
        private const val MAX_TOKENS = 600
        private const val MAX_HISTORY = 20
    }

    suspend fun sendMessage(
        userInput: String,
        history: List<Message>,
        level: ProficiencyLevel,
        apiKey: String,
        userName: String = "",
        learningGoals: String = "",
        interests: String = "",
        learnedWords: Set<String> = emptySet(),
        newWordsTarget: Int = 30,
        provider: LLMProvider = LLMProvider.CLAUDE,
        customModel: String = "",
        customBaseUrl: String = ""
    ): Result<CoachResponse> = withContext(Dispatchers.IO) {
        runCatching {
            val activeVocab = VocabularyRepository.getActiveVocabulary(
                level = level,
                learnedWords = learnedWords,
                newWordsTarget = newWordsTarget
            )

            val systemPrompt = VocabularyRepository.buildSystemPrompt(
                level = level,
                userName = userName,
                learningGoals = learningGoals,
                interests = interests,
                activeVocab = activeVocab
            )

            val messages = buildMessageHistory(history, userInput)

            val requestBody = ClaudeApiRequest(
                model = MODEL,
                max_tokens = MAX_TOKENS,
                system = systemPrompt,
                messages = messages
            )

            val body = json.encodeToString(requestBody)
                .toRequestBody("application/json".toMediaType())

            val request = Request.Builder()
                .url(API_URL)
                .post(body)
                .header("x-api-key", apiKey)
                .header("anthropic-version", "2023-06-01")
                .header("content-type", "application/json")
                .build()

            val response = client.newCall(request).execute()
            val responseText = response.body?.string()
                ?: throw Exception("Empty response from API")

            if (!response.isSuccessful) {
                throw Exception("API Error ${response.code}: $responseText")
            }

            val apiResponse = json.decodeFromString<ClaudeApiResponse>(responseText)
            val rawText = apiResponse.content.firstOrNull()?.text
                ?: throw Exception("No content in response")

            parseCoachResponse(rawText)
        }
    }

    private fun buildMessageHistory(history: List<Message>, newInput: String): List<ClaudeMessage> {
        val recent = history.takeLast(MAX_HISTORY)
        val messages = mutableListOf<ClaudeMessage>()

        for (msg in recent) {
            if (msg.isUser) {
                messages.add(ClaudeMessage(role = "user", content = msg.hanzi))
            } else {
                val assistantJson = buildString {
                    append("""{"hanzi":"${msg.hanzi}","pinyin":"${msg.pinyin}","english":"${msg.english}"""")
                    if (msg.tip != null) append(""","tip":"${msg.tip}"""")
                    append("}")
                }
                messages.add(ClaudeMessage(role = "assistant", content = assistantJson))
            }
        }

        messages.add(ClaudeMessage(role = "user", content = newInput))
        return messages
    }

    private fun parseCoachResponse(raw: String): CoachResponse {
        val cleaned = raw.trim()
            .removePrefix("```json").removePrefix("```")
            .removeSuffix("```").trim()

        return runCatching {
            json.decodeFromString<CoachResponse>(cleaned)
        }.getOrElse {
            val hanzi = extractField(cleaned, "hanzi") ?: cleaned
            val pinyin = extractField(cleaned, "pinyin") ?: ""
            val english = extractField(cleaned, "english") ?: ""
            val tip = extractField(cleaned, "tip")
            CoachResponse(hanzi = hanzi, pinyin = pinyin, english = english, tip = tip)
        }
    }

    private fun extractField(json: String, field: String): String? {
        val pattern = """"$field"\s*:\s*"([^"]*(?:\\.[^"]*)*)"""".toRegex()
        return pattern.find(json)?.groupValues?.getOrNull(1)
            ?.replace("\\\"", "\"")
            ?.replace("\\n", "\n")
    }
}
