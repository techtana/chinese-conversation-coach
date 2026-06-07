package com.mandarincoach.app.data.repository

import android.util.Log
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

class LLMRepository(private val prefs: UserPreferences? = null) {

    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .build()

    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        encodeDefaults = true
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
            Log.d("LLMRepository", "Starting sendMessage for provider: ${provider.name}")
            
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

            val url = when {
                provider == LLMProvider.CLAUDE -> LLMProvider.CLAUDE.baseUrl
                provider == LLMProvider.PRIVATE && customBaseUrl.isNotBlank() -> customBaseUrl
                provider.baseUrl.isNotBlank() -> provider.baseUrl
                else -> customBaseUrl
            }

            val model = if (customModel.trim().isNotBlank()) customModel.trim() else provider.defaultModel

            Log.d("LLMRepository", "Using URL: $url")
            Log.d("LLMRepository", "Using Model: $model")

            val request = if (provider == LLMProvider.CLAUDE) {
                buildClaudeRequest(url, apiKey, model, systemPrompt, history, userInput)
            } else {
                buildOpenAICompatibleRequest(url, apiKey, model, systemPrompt, history, userInput)
            }

            Log.d("LLMRepository", "Request Headers: ${request.headers}")

            val response = client.newCall(request).execute()
            val responseText = response.body?.string() ?: throw Exception("Empty response")

            Log.d("LLMRepository", "Response Code: ${response.code}")
            
            if (!response.isSuccessful) {
                Log.e("LLMRepository", "API Error: $responseText")
                throw Exception("API Error ${response.code}\nURL: ${request.url}\nModel: $model\nResponse: $responseText")
            }

            Log.d("LLMRepository", "Response Text: $responseText")

            if (provider == LLMProvider.CLAUDE) {
                val apiResponse = json.decodeFromString<ClaudeApiResponse>(responseText)
                apiResponse.usage?.let {
                    val cost = (it.input_tokens * provider.inputCostPerMillion + 
                               it.output_tokens * provider.outputCostPerMillion) / 1_000_000.0
                    prefs?.addCost(cost)
                }
                val rawText = apiResponse.content.firstOrNull()?.text ?: throw Exception("No content")
                parseCoachResponse(rawText)
            } else {
                val apiResponse = json.decodeFromString<OpenAIResponse>(responseText)
                apiResponse.usage?.let {
                    val cost = (it.prompt_tokens * provider.inputCostPerMillion + 
                               it.completion_tokens * provider.outputCostPerMillion) / 1_000_000.0
                    prefs?.addCost(cost)
                }
                val rawText = apiResponse.choices.firstOrNull()?.message?.content ?: throw Exception("No content")
                parseCoachResponse(rawText)
            }
        }
    }

    private fun buildClaudeRequest(
        url: String, 
        apiKey: String, 
        model: String, 
        system: String, 
        history: List<Message>, 
        userInput: String
    ): Request {
        val messages = history.takeLast(20).map { msg ->
            ClaudeMessage(role = if (msg.isUser) "user" else "assistant", content = msg.hanzi)
        } + ClaudeMessage(role = "user", content = userInput)

        val body = json.encodeToString(ClaudeApiRequest(model, 600, system, messages))
            .toRequestBody("application/json".toMediaType())

        return Request.Builder()
            .url(url)
            .post(body)
            .header("x-api-key", apiKey)
            .header("anthropic-version", "2023-06-01")
            .header("content-type", "application/json")
            .build()
    }

    private fun buildOpenAICompatibleRequest(
        url: String, 
        apiKey: String, 
        model: String, 
        system: String, 
        history: List<Message>, 
        userInput: String
    ): Request {
        val messages = mutableListOf(OpenAIMessage("system", system))
        messages.addAll(history.takeLast(20).map { msg ->
            OpenAIMessage(if (msg.isUser) "user" else "assistant", msg.hanzi)
        })
        messages.add(OpenAIMessage("user", userInput))

        val body = json.encodeToString(OpenAIRequest(model, messages))
            .toRequestBody("application/json".toMediaType())

        return Request.Builder()
            .url(url)
            .post(body)
            .header("Authorization", "Bearer $apiKey")
            .header("content-type", "application/json")
            .build()
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
