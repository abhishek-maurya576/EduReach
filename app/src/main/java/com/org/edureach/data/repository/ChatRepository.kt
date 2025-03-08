package com.org.edureach.data.repository

import android.content.Context
import com.org.edureach.data.database.AppDatabase
import com.org.edureach.data.database.daos.ChatSessionWithLastMessage
import com.org.edureach.data.database.entities.ChatMessageEntity
import com.org.edureach.data.database.entities.ChatSessionEntity
import com.org.edureach.viewmodels.AITutorForm
import com.org.edureach.viewmodels.ResponseLength
import kotlinx.coroutines.flow.Flow
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Repository for chat history operations
 */
class ChatRepository(context: Context) {
    private val chatDao = AppDatabase.getDatabase(context).chatDao()
    
    // Get all chat sessions
    fun getAllChatSessions(): Flow<List<ChatSessionEntity>> {
        return chatDao.getAllChatSessions()
    }
    
    // Get recent chat sessions with their last messages
    fun getRecentSessions(limit: Int = 10): Flow<List<ChatSessionWithLastMessage>> {
        return chatDao.getRecentSessionsWithLastMessage(limit)
    }
    
    // Get all messages for a specific session
    fun getChatMessagesForSession(sessionId: Long): Flow<List<ChatMessageEntity>> {
        return chatDao.getChatMessagesForSession(sessionId)
    }
    
    // Save a new chat session with the first message
    suspend fun saveChatSession(
        form: AITutorForm,
        response: String
    ): Long {
        val title = createSessionTitle(form)
        
        val session = ChatSessionEntity(
            subject = form.subject,
            topic = form.topic,
            subTopic = form.subTopic.takeIf { it.isNotBlank() },
            title = title
        )
        
        val message = ChatMessageEntity(
            sessionId = 0, // This will be updated in the transaction
            question = form.question.takeIf { it.isNotBlank() },
            response = response
        )
        
        return chatDao.createChatSessionWithMessage(session, message)
    }
    
    // Add a message to an existing session
    suspend fun addMessageToSession(
        sessionId: Long,
        question: String?,
        response: String
    ): Long {
        val message = ChatMessageEntity(
            sessionId = sessionId,
            question = question,
            response = response
        )
        
        return chatDao.insertChatMessage(message)
    }
    
    // Delete a chat session
    suspend fun deleteChatSession(sessionId: Long) {
        chatDao.deleteChatSession(sessionId)
    }
    
    // Delete all chat history
    suspend fun deleteAllChatHistory() {
        chatDao.deleteAllChatHistory()
    }
    
    // Get a specific chat session
    suspend fun getChatSession(sessionId: Long): ChatSessionEntity? {
        return chatDao.getChatSessionById(sessionId)
    }
    
    // Helper function to create a title for the session
    private fun createSessionTitle(form: AITutorForm): String {
        // If there's a question, use it (truncated)
        if (form.question.isNotBlank()) {
            val maxLength = 30
            return if (form.question.length > maxLength) {
                "${form.question.substring(0, maxLength)}..."
            } else {
                form.question
            }
        }
        
        // Otherwise use topic and subtopic
        return if (form.subTopic.isNotBlank()) {
            "${form.topic}: ${form.subTopic}"
        } else {
            form.topic
        }
    }
    
    // Format timestamp for display
    fun formatTimestamp(timestamp: Long): String {
        val sdf = SimpleDateFormat("MMM dd, yyyy • HH:mm", Locale.getDefault())
        return sdf.format(Date(timestamp))
    }
} 