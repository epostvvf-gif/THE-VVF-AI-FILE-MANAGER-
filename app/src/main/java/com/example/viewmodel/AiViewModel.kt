package com.example.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.model.ChatMessageEntity
import com.example.data.repository.GeminiRepository
import com.example.data.repository.SettingsRepository
import com.example.BuildConfig
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class AiViewModel(
    private val geminiRepository: GeminiRepository,
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    // --- Configurations ---
    val apiKey: StateFlow<String?> = settingsRepository.apiKeyFlow
        .map { savedKey ->
            if (!savedKey.isNullOrBlank()) {
                savedKey
            } else {
                val configKey = BuildConfig.GEMINI_API_KEY
                if (!configKey.isNullOrBlank() && configKey != "MY_GEMINI_API_KEY") {
                    configKey
                } else {
                    null
                }
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val isHighThinkingMode: StateFlow<Boolean> = settingsRepository.highThinkingModeFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    // Setup Panel visibility
    val isSetupPanelExpanded = MutableStateFlow(true)
    val apiKeyInput = MutableStateFlow("")
    val validationMessage = MutableStateFlow<String?>(null)

    // --- Chat Flow ---
    val chatMessages: StateFlow<List<ChatMessageEntity>> = geminiRepository.chatMessagesFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val isAiLoading = MutableStateFlow(false)

    init {
        viewModelScope.launch {
            // Seed welcome message if history is empty
            geminiRepository.chatMessagesFlow.collect { list ->
                if (list.isEmpty()) {
                    geminiRepository.insertMessage(
                        text = "Hello! I am your AI Storage Assistant. " +
                                "Configure your Gemini API Key in the panel above to ask me files and folder management questions, " +
                                "or let me analyze storage space structures for you!",
                        sender = "model"
                    )
                }
            }
        }
    }

    // --- Settings Actions ---
    fun applyApiKey(key: String) {
        if (key.isBlank()) {
            validationMessage.value = "API key cannot be empty."
            return
        }
        viewModelScope.launch {
            settingsRepository.saveApiKey(key)
            validationMessage.value = "API Key applied successfully!"
            isSetupPanelExpanded.value = false // collapse after success
        }
    }

    fun removeApiKey() {
        viewModelScope.launch {
            settingsRepository.clearApiKey()
            apiKeyInput.value = ""
            validationMessage.value = "API Key removed."
        }
    }

    fun toggleHighThinkingMode(enabled: Boolean) {
        viewModelScope.launch {
            settingsRepository.setHighThinkingMode(enabled)
        }
    }

    fun toggleSetupPanel() {
        isSetupPanelExpanded.value = !isSetupPanelExpanded.value
    }

    // --- Chat Actions ---
    fun sendMessage(text: String) {
        if (text.isBlank()) return

        val currentApiKey = apiKey.value
        if (currentApiKey.isNullOrBlank()) {
            viewModelScope.launch {
                geminiRepository.insertMessage(text = text, sender = "user")
                geminiRepository.insertMessage(
                    text = "⚠️ Gemini API Key is not configured. Please enter and apply your Gemini API Key in the 'Gemini AI Core Configuration' panel above to enable full file-level query reasoning.",
                    sender = "model"
                )
            }
            validationMessage.value = "Please add and apply your Gemini API Key first!"
            isSetupPanelExpanded.value = true
            return
        }

        viewModelScope.launch {
            // Save User message
            geminiRepository.insertMessage(text = text, sender = "user")
            isAiLoading.value = true

            // Send to Gemini API with full conversation context
            val history = chatMessages.value
            val response = geminiRepository.generateResponse(
                prompt = text,
                apiKey = currentApiKey,
                isHighThinkingMode = isHighThinkingMode.value,
                history = history
            )

            // Save AI message
            geminiRepository.insertMessage(text = response, sender = "model")
            isAiLoading.value = false
            validationMessage.value = null
        }
    }

    fun clearHistory() {
        viewModelScope.launch {
            geminiRepository.clearChatHistory()
            geminiRepository.insertMessage(
                text = "Chat history cleared. How can I help you manage your storage today?",
                sender = "model"
            )
        }
    }
}
