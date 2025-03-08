package com.org.edureach.data.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.ForeignKey
import androidx.room.Index
import java.util.Date

/**
 * Entity representing a chat session
 */
@Entity(
    tableName = "chat_sessions",
    indices = [Index("id")]
)
data class ChatSessionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val subject: String,
    val topic: String,
    val subTopic: String?,
    val timestamp: Long = System.currentTimeMillis(),
    val title: String // Topic or first few words of question
)

/**
 * Entity representing a chat message
 */
@Entity(
    tableName = "chat_messages",
    foreignKeys = [
        ForeignKey(
            entity = ChatSessionEntity::class,
            parentColumns = ["id"],
            childColumns = ["sessionId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("sessionId")]
)
data class ChatMessageEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val sessionId: Long,
    val question: String?,
    val response: String,
    val timestamp: Long = System.currentTimeMillis()
) 