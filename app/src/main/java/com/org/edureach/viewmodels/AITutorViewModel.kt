package com.org.edureach.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.org.edureach.network.GeminiApiService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class Message(
    val content: String,
    val isFromUser: Boolean,
    val timestamp: Long = System.currentTimeMillis()
)

class AITutorViewModel : ViewModel() {
    
    private val _messages = MutableStateFlow<List<Message>>(emptyList())
    val messages: StateFlow<List<Message>> = _messages.asStateFlow()
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    init {
        // Add a welcome message
        addMessage("Hello! I'm your AI tutor. How can I help you today?", false)
    }
    
    fun sendMessage(content: String) {
        if (content.isBlank()) return
        
        // Add user message
        addMessage(content, true)
        
        // Show loading state
        _isLoading.value = true
        
        // Process with Gemini API
        viewModelScope.launch {
            try {
                val response = getAIResponse(content)
                addMessage(response, false)
            } catch (e: Exception) {
                addMessage("Sorry, I encountered an error: ${e.message ?: "Unknown error"}", false)
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    private suspend fun getAIResponse(userMessage: String): String {
        return try {
            GeminiApiService.generateTutorResponse(userMessage)
        } catch (e: Exception) {
            "I'm having trouble connecting to my knowledge base. Please try again later."
        }
    }
    
    fun summarizeTopic(topic: String) {
        if (topic.isBlank()) {
            addMessage("Please provide a topic to summarize.", false)
            return
        }
        
        _isLoading.value = true
        
        viewModelScope.launch {
            try {
                val response = GeminiApiService.generateTopicSummary(topic)
                addMessage("Here's a summary of $topic:\n\n$response", false)
            } catch (e: Exception) {
                addMessage("Sorry, I couldn't summarize that topic: ${e.message ?: "Unknown error"}", false)
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    fun suggestResources(topic: String) {
        if (topic.isBlank()) {
            addMessage("Please provide a topic to suggest resources for.", false)
            return
        }
        
        _isLoading.value = true
        
        viewModelScope.launch {
            try {
                val response = GeminiApiService.generateResourceSuggestions(topic)
                addMessage("Here are some recommended resources for $topic:\n\n$response", false)
            } catch (e: Exception) {
                addMessage("Sorry, I couldn't suggest resources for that topic: ${e.message ?: "Unknown error"}", false)
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    fun explainConcept(concept: String) {
        if (concept.isBlank()) {
            addMessage("Please provide a concept to explain.", false)
            return
        }
        
        _isLoading.value = true
        
        viewModelScope.launch {
            try {
                val response = GeminiApiService.generateConceptExplanation(concept)
                addMessage("Here's an explanation of $concept:\n\n$response", false)
            } catch (e: Exception) {
                addMessage("Sorry, I couldn't explain that concept: ${e.message ?: "Unknown error"}", false)
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    private fun addMessage(content: String, isFromUser: Boolean) {
        val newMessage = Message(content, isFromUser)
        _messages.value = _messages.value + newMessage
    }
} 