package com.org.edureach.viewmodels

import android.content.Context
import android.content.Intent
import androidx.core.content.ContextCompat.startActivity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.org.edureach.data.database.daos.ChatSessionWithLastMessage
import com.org.edureach.data.database.entities.ChatMessageEntity
import com.org.edureach.data.repository.ChatRepository
import com.org.edureach.network.GeminiApiService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

enum class ResponseLength {
    BRIEF, SUMMARY, LONG
}

data class AITutorForm(
    val subject: String = "",
    val topic: String = "",
    val subTopic: String = "",
    val question: String = "",
    val responseLength: ResponseLength = ResponseLength.LONG
)

data class AITutorFormState(
    val form: AITutorForm = AITutorForm(),
    val response: String = "",
    val isLoading: Boolean = false,
    val error: String? = null,
    val currentSessionId: Long? = null,
    val recentSessions: List<ChatSessionWithLastMessage> = emptyList(),
    val historyExpanded: Boolean = false
)

class AITutorFormViewModel(private val context: Context) : ViewModel() {
    
    private val _state = MutableStateFlow(AITutorFormState())
    val state: StateFlow<AITutorFormState> = _state.asStateFlow()
    
    private val chatRepository = ChatRepository(context)
    
    init {
        // Load recent chat sessions
        loadRecentSessions()
    }
    
    private fun loadRecentSessions() {
        viewModelScope.launch {
            chatRepository.getRecentSessions().collect { sessions ->
                _state.value = _state.value.copy(
                    recentSessions = sessions
                )
            }
        }
    }
    
    fun toggleHistoryExpanded() {
        _state.value = _state.value.copy(
            historyExpanded = !_state.value.historyExpanded
        )
    }
    
    fun loadSession(sessionId: Long) {
        viewModelScope.launch {
            val session = chatRepository.getChatSession(sessionId)
            if (session != null) {
                // Update the form with the session data
                _state.value = _state.value.copy(
                    form = _state.value.form.copy(
                        subject = session.subject,
                        topic = session.topic,
                        subTopic = session.subTopic ?: ""
                    ),
                    currentSessionId = sessionId
                )
                
                // Load the latest message for this session
                chatRepository.getChatMessagesForSession(sessionId).collect { messages ->
                    if (messages.isNotEmpty()) {
                        val latestMessage = messages.maxByOrNull { it.timestamp }
                        _state.value = _state.value.copy(
                            form = _state.value.form.copy(
                                question = latestMessage?.question ?: ""
                            ),
                            response = latestMessage?.response ?: ""
                        )
                    }
                }
            }
        }
    }
    
    fun deleteSession(sessionId: Long) {
        viewModelScope.launch {
            chatRepository.deleteChatSession(sessionId)
            if (_state.value.currentSessionId == sessionId) {
                clearForm()
            }
        }
    }
    
    fun clearHistory() {
        viewModelScope.launch {
            chatRepository.deleteAllChatHistory()
            if (_state.value.currentSessionId != null) {
                clearForm()
            }
        }
    }
    
    fun updateSubject(subject: String) {
        _state.value = _state.value.copy(
            form = _state.value.form.copy(subject = subject)
        )
    }
    
    fun updateTopic(topic: String) {
        _state.value = _state.value.copy(
            form = _state.value.form.copy(topic = topic)
        )
    }
    
    fun updateSubTopic(subTopic: String) {
        _state.value = _state.value.copy(
            form = _state.value.form.copy(subTopic = subTopic)
        )
    }
    
    fun updateQuestion(question: String) {
        _state.value = _state.value.copy(
            form = _state.value.form.copy(question = question)
        )
    }
    
    fun updateResponseLength(responseLength: ResponseLength) {
        _state.value = _state.value.copy(
            form = _state.value.form.copy(responseLength = responseLength)
        )
    }
    
    fun clearForm() {
        _state.value = _state.value.copy(
            form = AITutorForm(),
            response = "",
            error = null,
            currentSessionId = null
        )
    }
    
    fun generateContent() {
        val currentForm = _state.value.form
        
        // Validate required fields
        if (currentForm.subject.isBlank() || currentForm.topic.isBlank()) {
            _state.value = _state.value.copy(
                error = "Subject and Topic are required fields."
            )
            return
        }
        
        // Clear previous errors and set loading state
        _state.value = _state.value.copy(
            isLoading = true,
            error = null
        )
        
        viewModelScope.launch {
            try {
                val response = GeminiApiService.generateStructuredResponse(
                    subject = currentForm.subject,
                    topic = currentForm.topic,
                    subTopic = currentForm.subTopic.takeIf { it.isNotBlank() },
                    question = currentForm.question.takeIf { it.isNotBlank() },
                    responseLength = currentForm.responseLength
                )
                
                // Save to database
                val currentSessionId = _state.value.currentSessionId
                if (currentSessionId != null) {
                    // Add to existing session
                    chatRepository.addMessageToSession(
                        sessionId = currentSessionId,
                        question = currentForm.question.takeIf { it.isNotBlank() },
                        response = response
                    )
                } else {
                    // Create new session
                    val newSessionId = chatRepository.saveChatSession(
                        form = currentForm,
                        response = response
                    )
                    _state.value = _state.value.copy(
                        currentSessionId = newSessionId
                    )
                }
                
                _state.value = _state.value.copy(
                    response = response,
                    isLoading = false
                )
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    error = "Error: ${e.message ?: "Unknown error"}",
                    isLoading = false
                )
            }
        }
    }
    
    fun shareContent(context: Context) {
        val content = _state.value.response
        if (content.isNotBlank()) {
            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_SUBJECT, "Educational Content from EduReach")
                putExtra(Intent.EXTRA_TEXT, content)
            }
            
            val chooserIntent = Intent.createChooser(shareIntent, "Share via")
            chooserIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            context.startActivity(chooserIntent)
        }
    }
    
    fun formatTimestamp(timestamp: Long): String {
        return chatRepository.formatTimestamp(timestamp)
    }
} 