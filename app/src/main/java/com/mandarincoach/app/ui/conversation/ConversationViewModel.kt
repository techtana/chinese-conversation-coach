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
import com.mandarincoach.app.data.model.Scenario
import com.mandarincoach.app.data.model.ScenarioItem
import com.mandarincoach.app.data.preferences.ProgressRepository
import com.mandarincoach.app.data.preferences.UserPreferences
import com.mandarincoach.app.data.repository.DictionaryRepository
import com.mandarincoach.app.data.repository.LLMRepository
import com.mandarincoach.app.data.repository.ScenarioRepository
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
    val isTranslating: Boolean = false,
    val scenario: Scenario? = null,
    val earnedItems: List<ScenarioItem> = emptyList(),
    val scenarioMood: String? = null,
    val showScenarioComplete: Boolean = false
)

class ConversationViewModel(application: Application) : AndroidViewModel(application) {

    init {
        Log.d("ConversationVM", "ViewModel Instance Created")
    }

    private val prefs = UserPreferences(application)
    private val progressRepo = ProgressRepository(application)
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

    fun initialize(level: ProficiencyLevel, scenarioId: String? = null) {
        Log.d("ConversationVM", "Initializing for level: $level, scenario: $scenarioId")
        currentLevel = level
        val scenario = scenarioId?.let { ScenarioRepository.byId(it) }
        _uiState.value = _uiState.value.copy(scenario = scenario)
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
        val scenario = _uiState.value.scenario
        val greeting = when {
            scenario != null -> "（${scenario.hanziTitle}——场景开始）"
            currentLevel == ProficiencyLevel.BEGINNER -> "你好！请用简单的中文和我练习。"
            currentLevel == ProficiencyLevel.INTERMEDIATE -> "你好！我想练习中文对话，可以从日常话题开始吗？"
            currentLevel == ProficiencyLevel.ADVANCED -> "你好！我想进行一次有深度的中文对话，可以聊聊文化或社会话题吗？"
            else -> "你好！咱们来一场自然流畅的中文对话吧，什么话题都行。"
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
                stage = currentStage,
                scenario = _uiState.value.scenario,
                earnedItems = _uiState.value.earnedItems.map { it.id }.toSet()
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
                val scenarioCompleted = handleScenarioEvent(response)
                onUserTurnCompleted(scenarioCompleted)
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
            pendingWordBank = null,
            earnedItems = emptyList(),
            scenarioMood = null,
            showScenarioComplete = false
        )
        if (apiKey.isNotBlank()) sendGreeting()
    }

    /**
     * Validates and applies a scenario event from the model: awards items,
     * updates the avatar mood, and records completion once every item has
     * been earned. Returns true when the scenario just completed.
     */
    private fun handleScenarioEvent(response: CoachResponse): Boolean {
        val scenario = _uiState.value.scenario ?: return false
        val rawEvent = response.scenario ?: return false

        val earnedIds = _uiState.value.earnedItems.map { it.id }.toSet()
        val event = scenario.validateEvent(rawEvent, earnedIds)

        val newItem = event.itemEarned?.let { id -> scenario.items.find { it.id == id } }
        if (newItem != null) {
            viewModelScope.launch { progressRepo.addEarnedItem(scenario.id, newItem.id) }
        }
        if (event.completed) {
            viewModelScope.launch { progressRepo.recordScenarioCompletion(scenario.id, currentStage.name) }
        }

        _uiState.value = _uiState.value.copy(
            earnedItems = _uiState.value.earnedItems + listOfNotNull(newItem),
            scenarioMood = event.mood ?: _uiState.value.scenarioMood,
            showScenarioComplete = _uiState.value.showScenarioComplete || event.completed
        )
        return event.completed
    }

    fun dismissScenarioComplete() {
        _uiState.value = _uiState.value.copy(showScenarioComplete = false)
    }

    /**
     * Counts successful user turns; once the session qualifies (enough
     * turns, or a completed scenario), records it and asks the progression
     * engine whether to offer the next stage.
     */
    private fun onUserTurnCompleted(scenarioCompleted: Boolean = false) {
        userTurnCount++
        if (qualifiedThisConversation) return
        if (currentLevel != ProficiencyLevel.BEGINNER || currentStage == BridgeStage.FREE_FLOW) return
        if (!scenarioCompleted && userTurnCount < StageProgressionEngine.QUALIFYING_TURNS) return

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
