package com.org.edureach.data.models

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

/**
 * Represents an EduWise coaching session
 */
@Entity(tableName = "eduwise_sessions")
data class EduWiseSession(
    @PrimaryKey val id: String,
    val userId: String,
    val title: String,
    val timestamp: Date,
    val lastQuery: String? = null,
    val lastResponse: String? = null,
    val learningStyle: LearningStyle? = null,
    val personality: EduWisePersonality = EduWisePersonality.SUPPORTIVE
)

/**
 * Learning style preferences for tailoring content delivery
 */
enum class LearningStyle {
    VISUAL,
    AUDITORY,
    KINESTHETIC,
    READING_WRITING,
    SIMPLE,
    UNKNOWN
}

/**
 * Teaching personality styles for the EduWise assistant
 */
enum class EduWisePersonality {
    SUPPORTIVE,  // Encouraging and positive
    CHALLENGING, // Pushes user to excel
    SOCRATIC     // Uses questions to guide learning
}

/**
 * Represents a message in an EduWise conversation
 */
@Entity(tableName = "eduwise_messages")
data class EduWiseMessage(
    @PrimaryKey val id: String,
    val sessionId: String,
    val content: String,
    val timestamp: Date,
    val isUserMessage: Boolean,
    val emotionalTone: EmotionalTone? = null
)

/**
 * Detected emotional tone of user messages
 */
enum class EmotionalTone {
    NEUTRAL,
    CONFUSED,
    FRUSTRATED,
    EXCITED,
    SATISFIED
}

/**
 * Tracks study recommendations and their completion status
 */
@Entity(tableName = "eduwise_recommendations")
data class StudyRecommendation(
    @PrimaryKey val id: String,
    val userId: String,
    val description: String,
    val createdDate: Date,
    val dueDate: Date? = null,
    val priority: Int = 2, // 1-3 scale
    val isCompleted: Boolean = false,
    val category: RecommendationType
)

/**
 * Types of recommendations EduWise can provide
 */
enum class RecommendationType {
    REVIEW_MATERIAL,
    PRACTICE_EXERCISE,
    NEW_CONCEPT,
    STUDY_TECHNIQUE,
    WELLBEING
} 