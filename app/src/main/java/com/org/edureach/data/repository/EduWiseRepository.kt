package com.org.edureach.data.repository

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.org.edureach.data.database.EduWiseDatabase
import com.org.edureach.data.models.*
import com.org.edureach.network.GeminiApiService
import kotlinx.coroutines.flow.Flow
import java.util.*
import kotlin.random.Random

/**
 * Repository for all EduWise-related data operations
 */
class EduWiseRepository(private val database: EduWiseDatabase) {
    
    private val sessionDao = database.eduWiseSessionDao()
    private val messageDao = database.eduWiseMessageDao()
    private val recommendationDao = database.studyRecommendationDao()
    private val auth = FirebaseAuth.getInstance()
    
    private val TAG = "EduWiseRepository"
    
    /**
     * Gets the current user ID or null if not logged in
     */
    private fun getCurrentUserId(): String? {
        return auth.currentUser?.uid
    }
    
    /**
     * Get all sessions for the current user
     */
    fun getAllSessions(): Flow<List<EduWiseSession>> {
        val userId = getCurrentUserId() ?: return kotlinx.coroutines.flow.flowOf(emptyList())
        return sessionDao.getAllSessionsForUser(userId)
    }
    
    /**
     * Create a new EduWise session
     */
    suspend fun createSession(title: String): String {
        val userId = getCurrentUserId() ?: throw IllegalStateException("User not logged in")
        val sessionId = UUID.randomUUID().toString()
        
        val session = EduWiseSession(
            id = sessionId,
            userId = userId,
            title = title,
            timestamp = Date()
        )
        
        sessionDao.insertSession(session)
        return sessionId
    }
    
    /**
     * Get session by ID
     */
    suspend fun getSessionById(sessionId: String): EduWiseSession? {
        return sessionDao.getSessionById(sessionId)
    }
    
    /**
     * Delete a session and all its messages
     */
    suspend fun deleteSession(sessionId: String) {
        val session = sessionDao.getSessionById(sessionId) ?: return
        sessionDao.deleteSession(session)
        messageDao.deleteAllMessagesForSession(sessionId)
    }
    
    /**
     * Get messages for a session
     */
    fun getMessagesForSession(sessionId: String): Flow<List<EduWiseMessage>> {
        return messageDao.getMessagesForSession(sessionId)
    }
    
    /**
     * Send a message to EduWise and get a response
     */
    suspend fun sendMessage(sessionId: String, message: String): EduWiseMessage {
        val session = sessionDao.getSessionById(sessionId) ?: throw IllegalArgumentException("Invalid session ID")
        
        // Create user message
        val userMessage = EduWiseMessage(
            id = UUID.randomUUID().toString(),
            sessionId = sessionId,
            content = message,
            timestamp = Date(),
            isUserMessage = true
        )
        
        messageDao.insertMessage(userMessage)
        
        // Detect emotional tone
        val emotionalTone = detectEmotionalTone(message)
        
        // Create prompt based on user's learning style and chosen personality
        val prompt = createEduWisePrompt(message, session.learningStyle, session.personality, emotionalTone)
        
        try {
            // Get response from Gemini
            val response = GeminiApiService.generateTutorResponse(prompt)
            
            // Create AI message
            val aiMessage = EduWiseMessage(
                id = UUID.randomUUID().toString(),
                sessionId = sessionId,
                content = response,
                timestamp = Date(),
                isUserMessage = false
            )
            
            messageDao.insertMessage(aiMessage)
            
            // Update session with last message/response
            sessionDao.updateSession(session.copy(
                lastQuery = message,
                lastResponse = response
            ))
            
            return aiMessage
        } catch (e: Exception) {
            Log.e(TAG, "Error getting response from Gemini", e)
            
            // Create error message
            val errorMessage = EduWiseMessage(
                id = UUID.randomUUID().toString(),
                sessionId = sessionId,
                content = "I'm sorry, I'm having trouble connecting right now. Please try again later.",
                timestamp = Date(),
                isUserMessage = false
            )
            
            messageDao.insertMessage(errorMessage)
            return errorMessage
        }
    }
    
    /**
     * Generate study recommendations based on user data
     */
    suspend fun generateStudyRecommendations(count: Int = 3) {
        val userId = getCurrentUserId() ?: return
        
        // In a real app, this would analyze user data to make personalized recommendations
        // For now, we'll create sample recommendations
        val recommendationTypes = RecommendationType.values()
        val today = Calendar.getInstance()
        
        repeat(count) { index ->
            val recommendationType = recommendationTypes[Random.nextInt(recommendationTypes.size)]
            val dueDate = Calendar.getInstance().apply {
                add(Calendar.DAY_OF_YEAR, Random.nextInt(1, 7))
            }.time
            
            val recommendation = StudyRecommendation(
                id = UUID.randomUUID().toString(),
                userId = userId,
                description = getSampleRecommendation(recommendationType),
                createdDate = Date(),
                dueDate = dueDate,
                priority = Random.nextInt(1, 4),
                category = recommendationType
            )
            
            recommendationDao.insertRecommendation(recommendation)
        }
    }
    
    /**
     * Get active recommendations for the current user
     */
    fun getActiveRecommendations(): Flow<List<StudyRecommendation>> {
        val userId = getCurrentUserId() ?: return kotlinx.coroutines.flow.flowOf(emptyList())
        return recommendationDao.getActiveRecommendationsForUser(userId)
    }
    
    /**
     * Mark a recommendation as complete
     */
    suspend fun completeRecommendation(recommendationId: String) {
        recommendationDao.markRecommendationComplete(recommendationId)
    }
    
    /**
     * Update the learning style for a user
     */
    suspend fun updateLearningStyle(learningStyle: LearningStyle) {
        val userId = getCurrentUserId() ?: return
        sessionDao.updateLearningStyleForUser(userId, learningStyle)
    }
    
    /**
     * Detect emotional tone in a message
     */
    private fun detectEmotionalTone(message: String): EmotionalTone {
        // Simple emotion detection based on keywords
        return when {
            message.contains(Regex("confused|don'?t understand|what\\?|unclear|lost", RegexOption.IGNORE_CASE)) -> 
                EmotionalTone.CONFUSED
            message.contains(Regex("frustrat(ed|ing)|annoying|difficult|hard|can'?t|stuck", RegexOption.IGNORE_CASE)) -> 
                EmotionalTone.FRUSTRATED
            message.contains(Regex("excited|amazing|love|great|awesome|wow", RegexOption.IGNORE_CASE)) -> 
                EmotionalTone.EXCITED
            message.contains(Regex("got it|understand|thanks|clear|helpful", RegexOption.IGNORE_CASE)) -> 
                EmotionalTone.SATISFIED
            else -> EmotionalTone.NEUTRAL
        }
    }
    
    /**
     * Create a prompt for Gemini API that incorporates the user's learning style
     */
    private fun createEduWisePrompt(
        message: String,
        learningStyle: LearningStyle?,
        personality: EduWisePersonality,
        emotionalTone: EmotionalTone?
    ): String {
        val styleGuidance = when (learningStyle) {
            LearningStyle.VISUAL -> "Include visual descriptions and imagery. Suggest diagrams or visual aids."
            LearningStyle.AUDITORY -> "Use explanations that focus on how things would sound or be explained verbally."
            LearningStyle.KINESTHETIC -> "Relate concepts to physical actions and real-world applications."
            LearningStyle.READING_WRITING -> "Structure responses with clear headings, lists, and written explanations."
            LearningStyle.SIMPLE -> "Use simple, straightforward language. Avoid complex terminologies and break down concepts into easy-to-understand parts."
            else -> ""
        }
        
        val personalityGuidance = when (personality) {
            EduWisePersonality.SUPPORTIVE -> "Be encouraging and supportive. Use positive reinforcement."
            EduWisePersonality.CHALLENGING -> "Challenge the student to think deeper. Ask follow-up questions."
            EduWisePersonality.SOCRATIC -> "Use questions to guide the learning process instead of direct answers."
        }
        
        val emotionalResponse = when (emotionalTone) {
            EmotionalTone.CONFUSED -> "The student seems confused. Provide clearer, simpler explanations."
            EmotionalTone.FRUSTRATED -> "The student seems frustrated. Be empathetic and offer encouragement with your answer."
            EmotionalTone.EXCITED -> "The student is excited. Match their enthusiasm in your response."
            EmotionalTone.SATISFIED -> "The student is satisfied with their progress. Acknowledge this and build upon it."
            else -> ""
        }
        
        return """
            You are EduWise, an educational AI coach focused on helping students learn effectively.
            
            $personalityGuidance
            
            $styleGuidance
            
            $emotionalResponse
            
            Focus on improving metacognitive abilities and learning strategies, not just providing answers.
            
            Student's message: $message
        """.trimIndent()
    }
    
    /**
     * Generate sample recommendations based on type
     */
    private fun getSampleRecommendation(type: RecommendationType): String {
        return when (type) {
            RecommendationType.REVIEW_MATERIAL -> "Review your notes on Python data structures to strengthen your understanding."
            RecommendationType.PRACTICE_EXERCISE -> "Complete 3 coding exercises on functions to build muscle memory."
            RecommendationType.NEW_CONCEPT -> "Explore the basics of machine learning to expand your knowledge."
            RecommendationType.STUDY_TECHNIQUE -> "Try the Pomodoro technique: 25 minutes of focused study followed by a 5-minute break."
            RecommendationType.WELLBEING -> "Take a 30-minute walk to refresh your mind before your next study session."
        }
    }
} 