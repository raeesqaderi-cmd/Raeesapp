package com.example.voice

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.util.Log
import java.util.Locale

class SpeechManager(
    private val context: Context,
    private val onStateChange: (SpeechState) -> Unit,
    private val onRmsChanged: (Float) -> Unit,
    private val onResult: (String) -> Unit,
    private val onError: (String) -> Unit
) : TextToSpeech.OnInitListener {

    private val TAG = "SpeechManager"
    private var tts: TextToSpeech? = null
    private var speechRecognizer: SpeechRecognizer? = null
    private var isTtsReady = false

    enum class SpeechState {
        IDLE,
        LISTENING,
        PROCESSING,
        SPEAKING
    }

    init {
        try {
            tts = TextToSpeech(context, this)
            initSpeechRecognizer()
        } catch (e: Exception) {
            Log.e(TAG, "Initialization failed", e)
            onError("Initialization error: ${e.localizedMessage}")
        }
    }

    private fun initSpeechRecognizer() {
        if (SpeechRecognizer.isRecognitionAvailable(context)) {
            speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context).apply {
                setRecognitionListener(object : RecognitionListener {
                    override fun onReadyForSpeech(params: Bundle?) {
                        Log.d(TAG, "onReadyForSpeech")
                        onStateChange(SpeechState.LISTENING)
                    }

                    override fun onBeginningOfSpeech() {
                        Log.d(TAG, "onBeginningOfSpeech")
                    }

                    override fun onRmsChanged(rmsdB: Float) {
                        onRmsChanged(rmsdB)
                    }

                    override fun onBufferReceived(buffer: ByteArray?) {}

                    override fun onEndOfSpeech() {
                        Log.d(TAG, "onEndOfSpeech")
                        onStateChange(SpeechState.PROCESSING)
                    }

                    override fun onError(error: Int) {
                        val message = when (error) {
                            SpeechRecognizer.ERROR_AUDIO -> "Audio recording error"
                            SpeechRecognizer.ERROR_CLIENT -> "Client-side error"
                            SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> "Insufficient permissions"
                            SpeechRecognizer.ERROR_NETWORK -> "Network error"
                            SpeechRecognizer.ERROR_NETWORK_TIMEOUT -> "Network timeout"
                            SpeechRecognizer.ERROR_NO_MATCH -> "No match found"
                            SpeechRecognizer.ERROR_RECOGNIZER_BUSY -> "Speech service is busy"
                            SpeechRecognizer.ERROR_SERVER -> "Server error"
                            SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "Speech timeout (No speech input)"
                            else -> "Unknown error"
                        }
                        Log.e(TAG, "STT Error: $error - $message")
                        onStateChange(SpeechState.IDLE)
                        onError(message)
                    }

                    override fun onResults(results: Bundle?) {
                        val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                        val text = matches?.firstOrNull() ?: ""
                        Log.d(TAG, "STT Result: $text")
                        onResult(text)
                    }

                    override fun onPartialResults(partialResults: Bundle?) {}

                    override fun onEvent(eventType: Int, params: Bundle?) {}
                })
            }
        } else {
            Log.e(TAG, "Speech Recognizer not available on this device")
            onError("Voice Speech Recognition is not available on this device.")
        }
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            tts?.let { t ->
                t.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                    override fun onStart(utteranceId: String?) {
                        onStateChange(SpeechState.SPEAKING)
                    }

                    override fun onDone(utteranceId: String?) {
                        onStateChange(SpeechState.IDLE)
                    }

                    override fun onError(utteranceId: String?) {
                        onStateChange(SpeechState.IDLE)
                        onError("TTS speaking failed")
                    }
                })
                isTtsReady = true
                Log.d(TAG, "TTS Initialized successfully")
            }
        } else {
            Log.e(TAG, "TTS Initialization failed")
            isTtsReady = false
        }
    }

    fun startListening() {
        try {
            stopSpeaking()
            val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                // Support multiple languages natively for continuous recognition
                putExtra(RecognizerIntent.EXTRA_LANGUAGE, "ps-AF") // Pashto default
                putExtra(RecognizerIntent.EXTRA_SUPPORTED_LANGUAGES, arrayListOf("ps", "fa", "prs", "en", "ar", "ur"))
                putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1)
            }
            speechRecognizer?.startListening(intent)
        } catch (e: Exception) {
            Log.e(TAG, "Error starting voice listening", e)
            onError(e.localizedMessage ?: "Could not start listening")
        }
    }

    fun stopListening() {
        try {
            speechRecognizer?.stopListening()
        } catch (e: Exception) {
            Log.e(TAG, "Error stopping voice listening", e)
        }
    }

    fun speak(text: String, languageCode: String = "ps") {
        if (!isTtsReady || tts == null) {
            Log.e(TAG, "TTS not ready yet")
            return
        }

        try {
            // Apply language rules and fallbacks
            val locale = when (languageCode.lowercase()) {
                "en" -> Locale.US
                "ar" -> Locale("ar")
                "ur" -> Locale("ur")
                "fa", "prs" -> Locale("fa") // Farsi / Dari fallback
                "ps" -> Locale("fa") // Use Farsi/Arabic phonetics if Pashto voice isn't present
                else -> Locale.getDefault()
            }

            tts?.language = locale
            tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, "RAEESTALK_SPEECH_ID")
        } catch (e: Exception) {
            Log.e(TAG, "TTS Speak error", e)
            onError("TTS error: ${e.localizedMessage}")
        }
    }

    fun stopSpeaking() {
        try {
            if (tts?.isSpeaking == true) {
                tts?.stop()
                onStateChange(SpeechState.IDLE)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error stopping TTS", e)
        }
    }

    fun onDestroy() {
        try {
            speechRecognizer?.destroy()
            tts?.stop()
            tts?.shutdown()
        } catch (e: Exception) {
            Log.e(TAG, "Error destroying speech components", e)
        }
    }
}
