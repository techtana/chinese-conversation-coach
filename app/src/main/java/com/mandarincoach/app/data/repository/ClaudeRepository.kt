package com.mandarincoach.app.data.repository

import com.mandarincoach.app.data.model.*

/**
 * Legacy wrapper for compatibility with older code.
 * Delegates to the new LLMRepository.
 */
class ClaudeRepository {
    private val delegate = LLMRepository()

    suspend fun sendMessage(
        userInput: String,
        history: List<Message>,
        level: ProficiencyLevel,
        apiKey: String,
        userName: String = "",
        learningGoals: String = "",
        interests: String = ""
    ): Result<CoachResponse> {
        return delegate.sendMessage(
            userInput = userInput,
            history = history,
            level = level,
            apiKey = apiKey,
            userName = userName,
            learningGoals = learningGoals,
            interests = interests,
            provider = LLMProvider.CLAUDE
        )
    }
}
