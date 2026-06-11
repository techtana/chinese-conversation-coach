package com.mandarincoach.app.ui.conversation

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.mandarincoach.app.data.model.BridgeStage
import com.mandarincoach.app.data.model.ChoiceOption
import com.mandarincoach.app.data.model.CoachResponse
import com.mandarincoach.app.data.model.Message
import com.mandarincoach.app.data.model.ProficiencyLevel
import com.mandarincoach.app.data.preferences.UserPreferences
import com.mandarincoach.app.data.repository.DictionaryRepository
import com.mandarincoach.app.data.repository.LLMRepository
import com.mandarincoach.app.data.repository.VocabularyRepository
import com.mandarincoach.app.domain.StageProgressionEngine
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
    val totalCost: Float = 0f,
    val inputMode: BridgeStage = BridgeStage.FREE_FLOW,
    val pendingChoices: List<ChoiceOption>? = null,
    val pendingWordBank: List<String>? = null,
    val stageAdvanceOffer: BridgeStage? = null,
    val assistTranslation: ChoiceOption? = null,
    val isTranslating: Boolean = false
)

class ConversationViewModel(application: Application) : AndroidViewModel(application) {

    init {
        Log.d("ConversationVM", "ViewModel Instance Created")
    }

    private val prefs = UserPreferences(application)
    private val repository = LLMRepository(prefs)

    private val _uiState = MutableStateFlow(ConversationUiState())
    val uiState: StateFlow<ConversationUiState> = _uiState.asStateFlow()

    private var currentLevel: ProficiencyLevel = ProficiencyLevel.BEGINNER
    private var apiKey: String = ""
    private var currentStage: BridgeStage = BridgeStage.FREE_FLOW
    private var userTurnCount = 0
    private var qualifiedThisConversation = false

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
        Log.d("ConversationVM", "Initializing for level: $level")
        currentLevel = level
        viewModelScope.launch {
            // The bridge only applies to beginners; follow stage changes
            // (e.g. manual override in settings) for the rest of the session.
            if (level == ProficiencyLevel.BEGINNER) {
                currentStage = prefs.bridgeStage.first()
                _uiState.value = _uiState.value.copy(inputMode = currentStage)
                launch {
                    prefs.bridgeStage.collect { stage ->
                        currentStage = stage
                        _uiState.value = _uiState.value.copy(inputMode = stage)
                    }
                }
            } else {
                currentStage = BridgeStage.FREE_FLOW
                _uiState.value = _uiState.value.copy(inputMode = BridgeStage.FREE_FLOW)
            }

            val provider = prefs.llmProvider.first()
            apiKey = prefs.getApiKeyForProvider(provider).first()
            Log.d("ConversationVM", "Using provider: ${provider.name}, Key length: ${apiKey.length}")

            if (apiKey.isBlank()) {
                _uiState.value = _uiState.value.copy(
                    error = "Please add your API key for ${provider.displayName} in Settings."
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

        val userMessage = Message(isUser = true, hanzi = text)
        val currentMessages = _uiState.value.messages + userMessage
        _uiState.value = _uiState.value.copy(
            messages = currentMessages,
            isLoading = true,
            error = null,
            pendingChoices = null,
            pendingWordBank = null
        )

        viewModelScope.launch {
            val provider = prefs.llmProvider.first()
            apiKey = prefs.getApiKeyForProvider(provider).first()
            
            if (apiKey.isBlank()) {
                _uiState.value = _uiState.value.copy(
                    error = "Please add your API key for ${provider.displayName} in Settings.",
                    isLoading = false
                )
                return@launch
            }
            val name = prefs.userName.first()
            val goals = prefs.learningGoals.first()
            val ints = prefs.interests.first()
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
                customBaseUrl = cUrl,
                stage = currentStage
            ).onSuccess { response ->
                // Auto-learn words used by the AI
                viewModelScope.launch {
                    val wordsInResponse = VocabularyRepository.segment(response.hanzi)
                    prefs.addLearnedWords(wordsInResponse)
                }

                val aiMessage = response.toMessage()
                // Null choices/wordBank in stage 1/2 means the model skipped
                // them — the UI falls back to free input for this turn.
                _uiState.value = _uiState.value.copy(
                    messages = currentMessages + aiMessage,
                    isLoading = false,
                    pendingChoices = response.choices?.shuffled(),
                    pendingWordBank = response.wordBank
                )
                onUserTurnCompleted()
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
        userTurnCount = 0
        qualifiedThisConversation = false
        _uiState.value = _uiState.value.copy(
            messages = emptyList(),
            error = null,
            pendingChoices = null,
            pendingWordBank = null
        )
        if (apiKey.isNotBlank()) sendGreeting()
    }

    /**
     * Counts successful user turns; once the session qualifies, records it
     * and asks the progression engine whether to offer the next stage.
     */
    private fun onUserTurnCompleted() {
        userTurnCount++
        if (qualifiedThisConversation) return
        if (currentLevel != ProficiencyLevel.BEGINNER || currentStage == BridgeStage.FREE_FLOW) return
        if (userTurnCount < StageProgressionEngine.QUALIFYING_TURNS) return

        qualifiedThisConversation = true
        viewModelScope.launch {
            prefs.incrementQualifyingSessions(currentStage)
            val decision = StageProgressionEngine.decide(
                currentStage = currentStage,
                qualifyingSessions = prefs.stageQualifyingSessions.first(),
                manuallyPinned = prefs.bridgeStageManual.first()
            )
            if (decision != null) {
                _uiState.value = _uiState.value.copy(stageAdvanceOffer = decision)
            }
        }
    }

    fun acceptStageAdvance() {
        val next = _uiState.value.stageAdvanceOffer ?: return
        _uiState.value = _uiState.value.copy(stageAdvanceOffer = null)
        viewModelScope.launch { prefs.setBridgeStage(next) }
    }

    fun declineStageAdvance() {
        _uiState.value = _uiState.value.copy(stageAdvanceOffer = null)
        viewModelScope.launch { prefs.setBridgeStageManual(true) }
    }

    /**
     * Inline EN→中 assist for scaffolded input: offline dictionary first,
     * micro LLM call as fallback.
     */
    fun translateFragment(word: String) {
        if (word.isBlank() || _uiState.value.isTranslating) return

        DictionaryRepository.reverseLookup(word)?.let { entry ->
            _uiState.value = _uiState.value.copy(
                assistTranslation = ChoiceOption(entry.hanzi, entry.pinyin, word)
            )
            return
        }

        _uiState.value = _uiState.value.copy(isTranslating = true)
        viewModelScope.launch {
            val provider = prefs.llmProvider.first()
            val key = prefs.getApiKeyForProvider(provider).first()
            val cModel = prefs.customModel.first()
            val cUrl = prefs.customBaseUrl.first()

            repository.translateFragment(word, key, provider, cModel, cUrl)
                .onSuccess { option ->
                    _uiState.value = _uiState.value.copy(assistTranslation = option, isTranslating = false)
                }
                .onFailure {
                    _uiState.value = _uiState.value.copy(isTranslating = false)
                }
        }
    }

    fun clearAssistTranslation() {
        _uiState.value = _uiState.value.copy(assistTranslation = null)
    }

    private fun CoachResponse.toMessage() = Message(
        isUser = false,
        hanzi = hanzi,
        pinyin = pinyin,
        english = english,
        tip = tip,
        correction = correction
    )
}
