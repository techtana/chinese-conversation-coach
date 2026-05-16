package com.mandarincoach.app.data.model

import kotlinx.serialization.Serializable

@Serializable
data class CoachResponse(
    val hanzi: String,
    val pinyin: String,
    val english: String,
    val tip: String? = null
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
    val content: List<ClaudeContent>
)

@Serializable
data class ClaudeContent(
    val type: String,
    val text: String
)
