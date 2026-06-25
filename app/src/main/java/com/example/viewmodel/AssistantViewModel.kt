package com.example.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.api.GeminiClient
import com.example.data.AppDatabase
import com.example.data.MessageEntity
import com.example.data.MessageRepository
import com.example.service.MessageInterceptorManager
import com.example.util.IntentExecutor
import com.example.voice.SpeechManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class AssistantViewModel(application: Application) : AndroidViewModel(application) {
    private val TAG = "AssistantViewModel"

    private val repository: MessageRepository
    val history: StateFlow<List<MessageEntity>>

    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    private var speechManager: SpeechManager? = null

    data class UiState(
        val speechState: SpeechManager.SpeechState = SpeechManager.SpeechState.IDLE,
        val rmsdB: Float = 0f,
        val currentInputText: String = "",
        val currentResponseText: String = "",
        val statusMessage: String = "",
        val isSmsReaderOn: Boolean = true,
        val isNotificationReaderOn: Boolean = true,
        val isRecordingPermissionGranted: Boolean = false
    )

    init {
        val database = AppDatabase.getDatabase(application)
        repository = MessageRepository(database.messageDao())
        
        history = repository.allMessages.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        // Read interceptor states
        _uiState.value = _uiState.value.copy(
            isSmsReaderOn = MessageInterceptorManager.isSmsReaderEnabled,
            isNotificationReaderOn = MessageInterceptorManager.isNotificationReaderEnabled
        )

        initSpeechManager()
    }

    private fun initSpeechManager() {
        speechManager = SpeechManager(
            context = getApplication(),
            onStateChange = { state ->
                viewModelScope.launch(Dispatchers.Main) {
                    _uiState.value = _uiState.value.copy(speechState = state)
                    if (state == SpeechManager.SpeechState.IDLE) {
                        _uiState.value = _uiState.value.copy(rmsdB = 0f)
                    }
                }
            },
            onRmsChanged = { rms ->
                viewModelScope.launch(Dispatchers.Main) {
                    _uiState.value = _uiState.value.copy(rmsdB = rms)
                }
            },
            onResult = { recognizedText ->
                viewModelScope.launch(Dispatchers.Main) {
                    _uiState.value = _uiState.value.copy(
                        currentInputText = recognizedText,
                        speechState = SpeechManager.SpeechState.PROCESSING
                    )
                    processCommand(recognizedText)
                }
            },
            onError = { err ->
                viewModelScope.launch(Dispatchers.Main) {
                    _uiState.value = _uiState.value.copy(
                        statusMessage = err,
                        speechState = SpeechManager.SpeechState.IDLE
                    )
                }
            }
        )
    }

    fun startListening() {
        _uiState.value = _uiState.value.copy(statusMessage = "", currentInputText = "", currentResponseText = "")
        speechManager?.startListening()
    }

    fun stopListening() {
        speechManager?.stopListening()
    }

    fun toggleSmsReader(enabled: Boolean) {
        MessageInterceptorManager.isSmsReaderEnabled = enabled
        _uiState.value = _uiState.value.copy(isSmsReaderOn = enabled)
    }

    fun toggleNotificationReader(enabled: Boolean) {
        MessageInterceptorManager.isNotificationReaderEnabled = enabled
        _uiState.value = _uiState.value.copy(isNotificationReaderOn = enabled)
    }

    fun setRecordingPermission(granted: Boolean) {
        _uiState.value = _uiState.value.copy(isRecordingPermissionGranted = granted)
    }

    fun processCommand(commandText: String) {
        if (commandText.trim().isEmpty()) return

        _uiState.value = _uiState.value.copy(
            currentInputText = commandText,
            speechState = SpeechManager.SpeechState.PROCESSING,
            statusMessage = "Analyzing voice command..."
        )

        viewModelScope.launch(Dispatchers.IO) {
            try {
                // Call Gemini for structured command understanding
                val result = GeminiClient.analyzeCommand(commandText)
                
                viewModelScope.launch(Dispatchers.Main) {
                    _uiState.value = _uiState.value.copy(
                        currentResponseText = result.response,
                        speechState = SpeechManager.SpeechState.SPEAKING,
                        statusMessage = "Executing action: ${result.action}"
                    )

                    // Speak out response
                    speechManager?.speak(result.response, result.language)

                    // Execute System intent if needed
                    val isActionTriggered = IntentExecutor.execute(
                        context = getApplication(),
                        action = result.action,
                        parameter = result.parameter
                    )

                    val actionStatus = if (isActionTriggered) result.action else "NONE"

                    // Log history to Room database
                    repository.insert(
                        MessageEntity(
                            request = commandText,
                            response = result.response,
                            detectedLanguage = result.language,
                            actionExecuted = actionStatus
                        )
                    )
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error processing speech command", e)
                viewModelScope.launch(Dispatchers.Main) {
                    _uiState.value = _uiState.value.copy(
                        statusMessage = "Error: ${e.localizedMessage}",
                        speechState = SpeechManager.SpeechState.IDLE
                    )
                }
            }
        }
    }

    fun deleteMessage(id: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteById(id)
        }
    }

    fun clearAllHistory() {
        viewModelScope.launch(Dispatchers.IO) {
            repository.clearAll()
        }
    }

    fun speakText(text: String, lang: String) {
        speechManager?.speak(text, lang)
    }

    override fun onCleared() {
        super.onCleared()
        speechManager?.onDestroy()
        MessageInterceptorManager.shutdown()
    }
}
