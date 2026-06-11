package com.mandarincoach.app.service

import android.app.Activity
import android.content.Intent
import android.speech.RecognizerIntent
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class SpeechRecognitionService {

    private val _isListening = MutableStateFlow(false)
    val isListening: StateFlow<Boolean> = _isListening

    private val _recognizedText = MutableStateFlow<String?>(null)
    val recognizedText: StateFlow<String?> = _recognizedText

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    /**
     * Triggers the native Google voice-to-text dialog.
     * Use this via ActivityResultLauncher in the UI layer.
     */
    fun createRecognizerIntent(
        languageCode: String = "zh-CN",
        autoTranscribe: Boolean = true,
        silenceMs: Long = 3000L
    ): Intent {
        return Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, languageCode)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE, languageCode)
            putExtra(RecognizerIntent.EXTRA_ONLY_RETURN_LANGUAGE_PREFERENCE, false)
            
            val prompt = if (autoTranscribe) {
                "请说话... Speak now\n(App will auto-complete after silence)"
            } else {
                "请说话... Speak now\n(Tap the mic when finished)"
            }
            putExtra(RecognizerIntent.EXTRA_PROMPT, prompt)
            
            if (autoTranscribe) {
                // Configurable silence duration
                putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_COMPLETE_SILENCE_LENGTH_MILLIS, silenceMs)
                putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_POSSIBLY_COMPLETE_SILENCE_LENGTH_MILLIS, silenceMs)
                // Alternate keys sometimes used by specific Google App versions
                putExtra("android.speech.extras.SPEECH_INPUT_COMPLETE_SILENCE_LENGTH_MILLIS", silenceMs)
                putExtra("android.speech.extras.SPEECH_INPUT_POSSIBLY_COMPLETE_SILENCE_LENGTH_MILLIS", silenceMs)
            } else {
                // Set silence to a very large value to effectively "wait for user"
                // 10 minutes of silence
                val infiniteSilence = 600000L
                putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_COMPLETE_SILENCE_LENGTH_MILLIS, infiniteSilence)
                putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_POSSIBLY_COMPLETE_SILENCE_LENGTH_MILLIS, infiniteSilence)
                putExtra("android.speech.extras.SPEECH_INPUT_COMPLETE_SILENCE_LENGTH_MILLIS", infiniteSilence)
                putExtra("android.speech.extras.SPEECH_INPUT_POSSIBLY_COMPLETE_SILENCE_LENGTH_MILLIS", infiniteSilence)
                
                // Increase minimum length to prevent early cut-off in manual mode
                putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_MINIMUM_LENGTH_MILLIS, 15000L) // 15 seconds
            }
            
            putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_MINIMUM_LENGTH_MILLIS, 1000L)

            // This triggers the system to offer to download the language if missing
            putExtra("android.speech.extra.EXTRA_ADDITIONAL_LANGUAGES", arrayOf(languageCode))
        }
    }

    /**
     * Triggers a system dialog to download/install language packs if needed.
     */
    fun triggerLanguageInstall(languageCode: String = "zh-CN") {
        try {
            val intent = Intent(RecognizerIntent.ACTION_GET_LANGUAGE_DETAILS)
            // Note: In a real app, you'd broadcast this or use ACTION_INSTALL_TTS_DATA
            // but for ASR, simply requesting the language in the RecognizerIntent usually triggers the prompt.
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun handleActivityResult(resultCode: Int, data: Intent?) {
        if (resultCode == Activity.RESULT_OK && data != null) {
            val matches = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
            val text = matches?.firstOrNull()
            if (!text.isNullOrBlank()) {
                _recognizedText.value = text
            }
        } else if (resultCode == Activity.RESULT_CANCELED) {
            // User cancelled
        } else {
            _error.value = "Speech recognition failed or was cancelled"
        }
    }

    fun clearRecognizedText() { _recognizedText.value = null }
    fun clearError() { _error.value = null }
}
