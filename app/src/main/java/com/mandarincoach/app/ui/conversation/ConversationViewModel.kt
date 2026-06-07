package com.mandarincoach.app.ui.conversation

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.mandarincoach.app.data.model.CoachResponse
import com.mandarincoach.app.data.model.Message
import com.mandarincoach.app.data.model.ProficiencyLevel
import com.mandarincoach.app.data.preferences.UserPreferences
import com.mandarincoach.app.data.repository.LLMRepository
import com.mandarincoach.app.data.repository.VocabularyRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

data class ConversationUiState(
    val messages: List<Message> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val speechSpeed: Float = 0.85f,
    val showEnglish: Boolean = true,
    val totalCost: Float = 0f
)

class ConversationViewModel(application: Application) : AndroidViewModel(application) {

    private val prefs = UserPreferences(application)
    private val repository = LLMRepository(prefs)

    private val _uiState = MutableStateFlow(ConversationUiState())
    val uiState: StateFlow<ConversationUiState> = _uiState.asStateFlow()

    private var currentLevel: ProficiencyLevel = ProficiencyLevel.BEGINNER
    private var apiKey: String = ""

    init {
        viewModelScope.launch {
            prefs.speechSpeed.collect { speed ->
                _uiState.value = _uiState.value.copy(speechSpeed = speed)
            }
        }
        viewModelScope.launch {
            prefs.showEnglish.collect { show ->
                _uiState.value = _uiState.value.copy(showEnglish = show)
            }
        }
        viewModelScope.launch {
            prefs.totalCost.collect { cost ->
                _uiState.value = _uiState.value.copy(totalCost = cost)
            }
        }
    }

    fun initialize(level: ProficiencyLevel) {
        currentLevel = level
        viewModelScope.launch {
            apiKey = prefs.apiKey.first()
            if (apiKey.isBlank()) {
                _uiState.value = _uiState.value.copy(
                    error = "Please add your Claude API key in Settings to start chatting."
                )
                return@launch
            }
            sendGreeting()
        }
    }

    private fun sendGreeting() {
        val greeting = when (currentLevel) {
            ProficiencyLevel.BEGINNER -> "你好！请用简单的中文和我练习。"
            ProficiencyLevel.INTERMEDIATE -> "你好！我想练习中文对话，可以从日常话题开始吗？"
            ProficiencyLevel.ADVANCED -> "你好！我想进行一次有深度的中文对话，可以聊聊文化或社会话题吗？"
            ProficiencyLevel.FLUENT -> "你好！咱们来一场自然流畅的中文对话吧，什么话题都行。"
        }
        sendMessage(greeting)
    }

    fun sendMessage(text: String) {
        if (text.isBlank()) return
        if (apiKey.isBlank()) {
            _uiState.value = _uiState.value.copy(
                error = "Please add your Claude API key in Settings."
            )
            return
        }

        val userMessage = Message(isUser = true, hanzi = text)
        val currentMessages = _uiState.value.messages + userMessage
        _uiState.value = _uiState.value.copy(messages = currentMessages, isLoading = true, error = null)

        viewModelScope.launch {
            val name = prefs.userName.first()
            val goals = prefs.learningGoals.first()
            val ints = prefs.interests.first()
            val provider = prefs.llmProvider.first()
            val cModel = prefs.customModel.first()
            val cUrl = prefs.customBaseUrl.first()
            val learned = prefs.learnedWords.first()
            val target = prefs.newWordsTarget.first()

            repository.sendMessage(
                userInput = text,
                history = currentMessages.dropLast(1),
                level = currentLevel,
                apiKey = apiKey,
                userName = name,
                learningGoals = goals,
                interests = ints,
                learnedWords = learned,
                newWordsTarget = target,
                provider = provider,
                customModel = cModel,
                customBaseUrl = cUrl
            ).onSuccess { response ->
                // Auto-learn words used by the AI
                viewModelScope.launch {
                    val wordsInResponse = VocabularyRepository.segment(response.hanzi)
                    prefs.addLearnedWords(wordsInResponse)
                }

                val aiMessage = response.toMessage()
                _uiState.value = _uiState.value.copy(
                    messages = currentMessages + aiMessage,
                    isLoading = false
                )
            }.onFailure { error ->
                _uiState.value = _uiState.value.copy(
                    messages = currentMessages,
                    isLoading = false,
                    error = error.message ?: "Something went wrong. Please try again."
                )
            }
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }

    fun clearConversation() {
        _uiState.value = _uiState.value.copy(messages = emptyList(), error = null)
        if (apiKey.isNotBlank()) sendGreeting()
    }

    private fun CoachResponse.toMessage() = Message(
        isUser = false,
        hanzi = hanzi,
        pinyin = pinyin,
        english = english,
        tip = tip
    )
}
