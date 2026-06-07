package com.mandarincoach.app.service

import android.content.Context
import android.content.Intent
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.util.Locale

class TextToSpeechService(private val context: Context) {

    private var tts: TextToSpeech? = null
    private var isReady = false
    private var pendingText: String? = null
    private var pendingSpeed: Float = 0.85f

    private val _isSpeaking = MutableStateFlow(false)
    val isSpeaking: StateFlow<Boolean> = _isSpeaking

    init {
        tts = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                setupMandarin()
            }
        }
    }

    private fun setupMandarin() {
        val locale = Locale.SIMPLIFIED_CHINESE
        val result = tts?.setLanguage(locale)

        if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
            // Trigger system to install Mandarin data
            triggerLanguageInstall()
            isReady = false
        } else {
            // Success - try to pick a high quality local voice if available
            try {
                val bestVoice = tts?.voices?.filter {
                    it.locale == locale && !it.isNetworkConnectionRequired
                }?.maxByOrNull { it.quality } ?: tts?.defaultVoice

                bestVoice?.let { tts?.voice = it }
            } catch (ignore: Exception) {
                // Fallback to default if voice selection fails
            }

            isReady = true
            tts?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                override fun onStart(utteranceId: String?) { _isSpeaking.value = true }
                override fun onDone(utteranceId: String?) { _isSpeaking.value = false }
                @Deprecated("Deprecated in Java")
                override fun onError(utteranceId: String?) { _isSpeaking.value = false }
            })

            pendingText?.let { speak(it, pendingSpeed) }
            pendingText = null
        }
    }

    private fun triggerLanguageInstall() {
        val installIntent = Intent()
        installIntent.action = TextToSpeech.Engine.ACTION_INSTALL_TTS_DATA
        installIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        context.startActivity(installIntent)
    }

    fun speak(text: String, speed: Float = 0.85f) {
        if (!isReady) {
            pendingText = text
            pendingSpeed = speed
            return
        }
        tts?.setSpeechRate(speed)
        tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, "mandarin_coach_${System.currentTimeMillis()}")
    }

    fun stop() {
        tts?.stop()
        _isSpeaking.value = false
    }

    fun shutdown() {
        tts?.stop()
        tts?.shutdown()
        tts = null
    }
}
