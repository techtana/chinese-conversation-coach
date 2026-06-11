package com.mandarincoach.app.data.model

import kotlinx.serialization.Serializable

@Serializable
data class ChoiceOption(
    val hanzi: String,
    val pinyin: String = "",
    val english: String = ""
)

@Serializable
data class Correction(
    val userSaid: String,
    val better: String,
    val note: String = ""
)

@Serializable
data class ScenarioEvent(
    val itemEarned: String? = null,
    val mood: String? = null,
    val completed: Boolean = false
)

@Serializable
data class CoachResponse(
    val hanzi: String,
    val pinyin: String,
    val english: String,
    val tip: String? = null,
    val choices: List<ChoiceOption>? = null,
    val wordBank: List<String>? = null,
    val expectedAnswer: String? = null,
    val correction: Correction? = null,
    val scenario: ScenarioEvent? = null
)

@Serializable
data class ClaudeApiRequest(
    val model: String,
    val max_tokens: Int,
    val system: String,
    val messages: List<ClaudeMessage>
)

@Serializable
data class ClaudeMessage(
    val role: String,
    val content: String
)

@Serializable
data class ClaudeApiResponse(
    val content: List<ClaudeContent>,
    val usage: ClaudeUsage? = null
)

@Serializable
data class ClaudeUsage(
    val input_tokens: Int = 0,
    val output_tokens: Int = 0
)

@Serializable
data class ClaudeContent(
    val type: String,
    val text: String
)

@Serializable
data class OpenAIRequest(
    val model: String,
    val messages: List<OpenAIMessage>,
    val response_format: OpenAIResponseFormat? = null
)

@Serializable
data class OpenAIMessage(
    val role: String,
    val content: String
)

@Serializable
data class OpenAIResponseFormat(
    val type: String
)

@Serializable
data class OpenAIResponse(
    val choices: List<OpenAIChoice>,
    val usage: OpenAIUsage? = null
)

@Serializable
data class OpenAIChoice(
    val message: OpenAIMessage
)

@Serializable
data class OpenAIUsage(
    val prompt_tokens: Int,
    val completion_tokens: Int
)
