package com.mandarincoach.app.data.repository

import com.mandarincoach.app.data.model.ClaudeApiRequest
import com.mandarincoach.app.data.model.ClaudeApiResponse
import com.mandarincoach.app.data.model.ClaudeMessage
import com.mandarincoach.app.data.model.CoachResponse
import com.mandarincoach.app.data.model.Message
import com.mandarincoach.app.data.model.ProficiencyLevel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.util.concurrent.TimeUnit

class ClaudeRepository {

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
        private const val MODEL = "claude-haiku-4-5-20251001"
        private const val MAX_TOKENS = 600
        private const val MAX_HISTORY = 20
    }

    suspend fun sendMessage(
        userInput: String,
        history: List<Message>,
        level: ProficiencyLevel,
        apiKey: String
    ): Result<CoachResponse> = withContext(Dispatchers.IO) {
        runCatching {
            val systemPrompt = VocabularyRepository.buildSystemPrompt(level)

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
                val errorMsg = runCatching {
                    json.parseToJsonElement(responseText)
                        .let { it.toString() }
                }.getOrDefault(responseText)
                throw Exception("API error ${response.code}: $errorMsg")
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
