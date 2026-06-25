package com.example.service

import android.content.Context
import android.speech.tts.TextToSpeech
import android.util.Log
import java.util.Locale

object MessageInterceptorManager {
    private const val TAG = "InterceptorManager"

    var isSmsReaderEnabled = true
    var isNotificationReaderEnabled = true

    private var backgroundTts: TextToSpeech? = null
    private var isTtsReady = false

    fun init(context: Context) {
        if (backgroundTts == null) {
            try {
                backgroundTts = TextToSpeech(context.applicationContext) { status ->
                    if (status == TextToSpeech.SUCCESS) {
                        isTtsReady = true
                        Log.d(TAG, "Background TTS successfully initialized")
                    } else {
                        Log.e(TAG, "Background TTS initialization failed")
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Failed to create background TTS", e)
            }
        }
    }

    fun handleInterceptedMessage(sender: String, messageText: String) {
        Log.d(TAG, "Intercepted from $sender: $messageText")
        
        // Detect language heuristically to use correct phonetic TTS voice
        val lang = detectLanguageHeuristically(messageText)
        val textToSpeak = when (lang) {
            "ps" -> "د $sender څخه نوی پیغام: $messageText"
            "prs", "fa" -> "پیام جدید از $sender: $messageText"
            "ur" -> "$sender کی طرف سے نیا پیغام: $messageText"
            "ar" -> "رسالة جديدة من $sender: $messageText"
            else -> "New message from $sender: $messageText"
        }

        speakInBackground(textToSpeak, lang)
    }

    private fun speakInBackground(text: String, lang: String) {
        if (!isTtsReady || backgroundTts == null) {
            Log.e(TAG, "Background TTS not ready. Cannot speak intercepted message.")
            return
        }

        try {
            val locale = when (lang) {
                "en" -> Locale.US
                "ar" -> Locale("ar")
                "ur" -> Locale("ur")
                "fa", "prs" -> Locale("fa")
                "ps" -> Locale("fa") // Phonetic fallback
                else -> Locale.getDefault()
            }

            backgroundTts?.language = locale
            backgroundTts?.speak(text, TextToSpeech.QUEUE_ADD, null, "INTERCEPT_TTS_ID")
        } catch (e: Exception) {
            Log.e(TAG, "Error in background TTS speak", e)
        }
    }

    private fun detectLanguageHeuristically(text: String): String {
        // Simple heuristic based on character blocks
        var arabicCharCount = 0
        var englishCharCount = 0
        for (char in text) {
            val code = char.code
            if (code in 0x0600..0x06FF || code in 0x0750..0x077F || code in 0x08A0..0x08FF) {
                arabicCharCount++
            } else if (char.isLetter()) {
                englishCharCount++
            }
        }

        return if (arabicCharCount > englishCharCount) {
            // Refine to specific RTL languages
            if (text.contains("ښ") || text.contains("ځ") || text.contains("ړ") || text.contains("ټ") || text.contains("ډ")) {
                "ps" // Pashto unique letters
            } else if (text.contains("گ") || text.contains("پ") || text.contains("چ") || text.contains("ژ")) {
                "fa" // Farsi/Dari unique letters
            } else if (text.contains("ٹ") || text.contains("ڈ") || text.contains("ڑ") || text.contains("ے") || text.contains("ں")) {
                "ur" // Urdu unique letters
            } else {
                "ar" // Standard Arabic
            }
        } else {
            "en"
        }
    }

    fun shutdown() {
        try {
            backgroundTts?.stop()
            backgroundTts?.shutdown()
            backgroundTts = null
            isTtsReady = false
        } catch (e: Exception) {
            Log.e(TAG, "Error shutting down background TTS", e)
        }
    }
}
