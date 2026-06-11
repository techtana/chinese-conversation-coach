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
        customBaseUrl: String = "",
        stage: BridgeStage = BridgeStage.FREE_FLOW
    ): Result<CoachResponse> = withContext(Dispatchers.IO) {
        runCatching {
            Log.d("LLMRepository", "Starting sendMessage for provider: ${provider.name}")

            val activeVocab = VocabularyRepository.getActiveVocabulary(
                level = level,
                learnedWords = learnedWords,
                newWordsTarget = newWordsTarget
            )

            val systemPrompt = PromptBuilder.build(
                level = level,
                userName = userName,
                learningGoals = learningGoals,
                interests = interests,
                activeVocab = activeVocab,
                stage = stage
            )

            // Choice lists and word banks consume extra output tokens
            val maxTokens = when (stage) {
                BridgeStage.PASSIVE_INPUT, BridgeStage.FRAGMENT_BUILDING -> 1000
                else -> 600
            }

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
                buildClaudeRequest(url, apiKey, model, systemPrompt, history, userInput, maxTokens)
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
                CoachResponseParser.parse(rawText)
            } else {
                val apiResponse = json.decodeFromString<OpenAIResponse>(responseText)
                apiResponse.usage?.let {
                    val cost = (it.prompt_tokens * provider.inputCostPerMillion + 
                               it.completion_tokens * provider.outputCostPerMillion) / 1_000_000.0
                    prefs?.addCost(cost)
                }
                val rawText = apiResponse.choices.firstOrNull()?.message?.content ?: throw Exception("No content")
                CoachResponseParser.parse(rawText)
            }
        }
    }

    private fun buildClaudeRequest(
        url: String,
        apiKey: String,
        model: String,
        system: String,
        history: List<Message>,
        userInput: String,
        maxTokens: Int
    ): Request {
        val messages = history.takeLast(20).map { msg ->
            ClaudeMessage(role = if (msg.isUser) "user" else "assistant", content = msg.hanzi)
        } + ClaudeMessage(role = "user", content = userInput)

        val body = json.encodeToString(ClaudeApiRequest(model, maxTokens, system, messages))
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

}
