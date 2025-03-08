package com.org.edureach.data.database.daos

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import com.org.edureach.data.database.entities.ChatMessageEntity
import com.org.edureach.data.database.entities.ChatSessionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ChatDao {
    // Session operations
    @Insert
    suspend fun insertChatSession(session: ChatSessionEntity): Long
    
    @Query("SELECT * FROM chat_sessions ORDER BY timestamp DESC")
    fun getAllChatSessions(): Flow<List<ChatSessionEntity>>
    
    @Query("SELECT * FROM chat_sessions WHERE id = :sessionId")
    suspend fun getChatSessionById(sessionId: Long): ChatSessionEntity?
    
    @Query("DELETE FROM chat_sessions WHERE id = :sessionId")
    suspend fun deleteChatSession(sessionId: Long)
    
    // Message operations
    @Insert
    suspend fun insertChatMessage(message: ChatMessageEntity): Long
    
    @Query("SELECT * FROM chat_messages WHERE sessionId = :sessionId ORDER BY timestamp ASC")
    fun getChatMessagesForSession(sessionId: Long): Flow<List<ChatMessageEntity>>
    
    @Query("SELECT * FROM chat_messages WHERE sessionId = :sessionId ORDER BY timestamp DESC LIMIT 1")
    suspend fun getLatestMessageForSession(sessionId: Long): ChatMessageEntity?
    
    // Transaction to create a new chat session with the first message
    @Transaction
    suspend fun createChatSessionWithMessage(
        session: ChatSessionEntity,
        message: ChatMessageEntity
    ): Long {
        val sessionId = insertChatSession(session)
        insertChatMessage(message.copy(sessionId = sessionId))
        return sessionId
    }
    
    // Query to get the most recent sessions with their latest messages
    @Query("""
        SELECT s.*, m.response as lastResponse 
        FROM chat_sessions s 
        LEFT JOIN (
            SELECT sessionId, MAX(timestamp) as maxTime 
            FROM chat_messages 
            GROUP BY sessionId
        ) as latest ON s.id = latest.sessionId
        LEFT JOIN chat_messages m ON latest.sessionId = m.sessionId AND latest.maxTime = m.timestamp
        ORDER BY s.timestamp DESC 
        LIMIT :limit
    """)
    fun getRecentSessionsWithLastMessage(limit: Int = 10): Flow<List<ChatSessionWithLastMessage>>
    
    // Delete all history
    @Query("DELETE FROM chat_sessions")
    suspend fun deleteAllChatHistory()
}

// Data class to hold the join query result
data class ChatSessionWithLastMessage(
    val id: Long,
    val subject: String,
    val topic: String,
    val subTopic: String?,
    val timestamp: Long,
    val title: String,
    val lastResponse: String?
) 