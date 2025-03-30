package com.org.edureach.viewmodels

import android.content.Context
import android.content.Intent
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.org.edureach.data.database.EduWiseDatabase
import com.org.edureach.data.models.*
import com.org.edureach.data.repository.EduWiseRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

/**
 * State class for the EduWise UI
 */
data class EduWiseState(
    val sessions: List<EduWiseSession> = emptyList(),
    val currentSessionId: String? = null,
    val messages: List<EduWiseMessage> = emptyList(),
    val recommendations: List<StudyRecommendation> = emptyList(),
    val learningStyle: LearningStyle? = null,
    val personality: EduWisePersonality = EduWisePersonality.SUPPORTIVE,
    val isLoading: Boolean = false,
    val error: String? = null,
    val showSessionList: Boolean = false,
    val showStyleAssessment: Boolean = false,
    val showRecommendations: Boolean = false
)

/**
 * ViewModel for the EduWise feature
 */
class EduWiseViewModel(context: Context) : ViewModel() {
    
    private val repository: EduWiseRepository
    
    private val _state = MutableStateFlow(EduWiseState())
    val state: StateFlow<EduWiseState> = _state.asStateFlow()
    
    init {
        val database = EduWiseDatabase.getDatabase(context)
        repository = EduWiseRepository(database)
        
        // Load sessions
        viewModelScope.launch {
            repository.getAllSessions()
                .collect { sessions ->
                    _state.update { it.copy(sessions = sessions) }
                    
                    // If there are sessions but no current session is selected,
                    // select the most recent one (first in the list since they're sorted by timestamp DESC)
                    if (sessions.isNotEmpty() && _state.value.currentSessionId == null) {
                        selectSession(sessions[0].id)
                    }
                }
        }
        
        // Load recommendations
        viewModelScope.launch {
            repository.getActiveRecommendations()
                .collect { recommendations ->
                    _state.update { it.copy(recommendations = recommendations) }
                }
        }
    }
    
    /**
     * Create a new session
     */
    fun createSession(title: String) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            try {
                val sessionId = repository.createSession(title)
                
                // First, load the messages to ensure they're ready when state updates
                repository.getMessagesForSession(sessionId)
                    .take(1)
                    .collect { messages ->
                        // Then update the state with everything at once
                        _state.update { 
                            it.copy(
                                currentSessionId = sessionId,
                                messages = messages,
                                showSessionList = false,
                                isLoading = false
                            )
                        }
                    }
                
                // Start continuous message monitoring
                loadMessages(sessionId)
            } catch (e: Exception) {
                _state.update { 
                    it.copy(
                        error = "Failed to create session: ${e.message}",
                        isLoading = false
                    )
                }
            }
        }
    }
    
    /**
     * Select an existing session
     */
    fun selectSession(sessionId: String) {
        viewModelScope.launch {
            _state.update { 
                it.copy(
                    currentSessionId = sessionId,
                    showSessionList = false
                )
            }
            loadMessages(sessionId)
        }
    }
    
    /**
     * Load messages for the current session
     */
    private fun loadMessages(sessionId: String) {
        viewModelScope.launch {
            repository.getMessagesForSession(sessionId)
                .collect { messages ->
                    _state.update { it.copy(messages = messages) }
                }
        }
    }
    
    /**
     * Send a message to EduWise
     */
    fun sendMessage(message: String) {
        val sessionId = _state.value.currentSessionId ?: return
        
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            try {
                repository.sendMessage(sessionId, message)
                _state.update { it.copy(isLoading = false) }
            } catch (e: Exception) {
                _state.update { 
                    it.copy(
                        error = "Failed to send message: ${e.message}",
                        isLoading = false
                    )
                }
            }
        }
    }
    
    /**
     * Delete the current session
     */
    fun deleteCurrentSession() {
        val sessionId = _state.value.currentSessionId ?: return
        
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            try {
                repository.deleteSession(sessionId)
                _state.update { 
                    it.copy(
                        currentSessionId = null,
                        messages = emptyList(),
                        isLoading = false
                    )
                }
            } catch (e: Exception) {
                _state.update { 
                    it.copy(
                        error = "Failed to delete session: ${e.message}",
                        isLoading = false
                    )
                }
            }
        }
    }
    
    /**
     * Generate study recommendations
     */
    fun generateRecommendations() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            try {
                repository.generateStudyRecommendations()
                _state.update { it.copy(isLoading = false) }
            } catch (e: Exception) {
                _state.update { 
                    it.copy(
                        error = "Failed to generate recommendations: ${e.message}",
                        isLoading = false
                    )
                }
            }
        }
    }
    
    /**
     * Mark a recommendation as complete
     */
    fun completeRecommendation(recommendationId: String) {
        viewModelScope.launch {
            repository.completeRecommendation(recommendationId)
        }
    }
    
    /**
     * Update the learning style
     */
    fun updateLearningStyle(learningStyle: LearningStyle) {
        viewModelScope.launch {
            repository.updateLearningStyle(learningStyle)
            _state.update { it.copy(learningStyle = learningStyle) }
        }
    }
    
    /**
     * Update the personality
     */
    fun updatePersonality(personality: EduWisePersonality) {
        _state.update { it.copy(personality = personality) }
    }
    
    /**
     * Toggle visibility of the session list
     */
    fun toggleSessionList() {
        _state.update { it.copy(showSessionList = !it.showSessionList) }
    }
    
    /**
     * Toggle visibility of the style assessment
     */
    fun toggleStyleAssessment() {
        _state.update { it.copy(showStyleAssessment = !it.showStyleAssessment) }
    }
    
    /**
     * Toggle visibility of recommendations
     */
    fun toggleRecommendations() {
        _state.update { it.copy(showRecommendations = !it.showRecommendations) }
    }
    
    /**
     * Clear error message
     */
    fun clearError() {
        _state.update { it.copy(error = null) }
    }
    
    /**
     * Format date for display
     */
    fun formatDate(date: Date): String {
        val formatter = SimpleDateFormat("MMM d, yyyy", Locale.getDefault())
        return formatter.format(date)
    }
    
    /**
     * Share session content
     */
    fun shareSession(context: Context) {
        val sessionId = _state.value.currentSessionId ?: return
        val session = _state.value.sessions.find { it.id == sessionId } ?: return
        
        val shareIntent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_SUBJECT, "My EduWise Session: ${session.title}")
            
            val messagesText = _state.value.messages.joinToString("\n\n") { message ->
                val prefix = if (message.isUserMessage) "Me: " else "EduWise: "
                "$prefix${message.content}"
            }
            
            putExtra(Intent.EXTRA_TEXT, messagesText)
            type = "text/plain"
        }
        
        context.startActivity(Intent.createChooser(shareIntent, "Share via"))
    }
} 