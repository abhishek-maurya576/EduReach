package com.org.edureach.data.database

import androidx.room.*
import com.org.edureach.data.models.EduWiseMessage
import com.org.edureach.data.models.EduWiseSession
import com.org.edureach.data.models.LearningStyle
import com.org.edureach.data.models.StudyRecommendation
import kotlinx.coroutines.flow.Flow
import java.util.Date

/**
 * Data Access Object for EduWise sessions
 */
@Dao
interface EduWiseSessionDao {
    @Query("SELECT * FROM eduwise_sessions WHERE userId = :userId ORDER BY timestamp DESC")
    fun getAllSessionsForUser(userId: String): Flow<List<EduWiseSession>>
    
    @Query("SELECT * FROM eduwise_sessions WHERE id = :sessionId")
    suspend fun getSessionById(sessionId: String): EduWiseSession?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSession(session: EduWiseSession)
    
    @Update
    suspend fun updateSession(session: EduWiseSession)
    
    @Delete
    suspend fun deleteSession(session: EduWiseSession)
    
    @Query("DELETE FROM eduwise_sessions WHERE userId = :userId")
    suspend fun deleteAllSessionsForUser(userId: String)
    
    @Query("UPDATE eduwise_sessions SET learningStyle = :learningStyle WHERE userId = :userId")
    suspend fun updateLearningStyleForUser(userId: String, learningStyle: LearningStyle)
}

/**
 * Data Access Object for EduWise messages
 */
@Dao
interface EduWiseMessageDao {
    @Query("SELECT * FROM eduwise_messages WHERE sessionId = :sessionId ORDER BY timestamp ASC")
    fun getMessagesForSession(sessionId: String): Flow<List<EduWiseMessage>>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(message: EduWiseMessage)
    
    @Query("DELETE FROM eduwise_messages WHERE sessionId = :sessionId")
    suspend fun deleteAllMessagesForSession(sessionId: String)
}

/**
 * Data Access Object for study recommendations
 */
@Dao
interface StudyRecommendationDao {
    @Query("SELECT * FROM eduwise_recommendations WHERE userId = :userId AND isCompleted = 0 ORDER BY priority DESC, dueDate ASC")
    fun getActiveRecommendationsForUser(userId: String): Flow<List<StudyRecommendation>>
    
    @Query("SELECT * FROM eduwise_recommendations WHERE userId = :userId AND isCompleted = 1 ORDER BY dueDate DESC LIMIT 20")
    fun getCompletedRecommendationsForUser(userId: String): Flow<List<StudyRecommendation>>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecommendation(recommendation: StudyRecommendation)
    
    @Update
    suspend fun updateRecommendation(recommendation: StudyRecommendation)
    
    @Delete
    suspend fun deleteRecommendation(recommendation: StudyRecommendation)
    
    @Query("UPDATE eduwise_recommendations SET isCompleted = 1 WHERE id = :recommendationId")
    suspend fun markRecommendationComplete(recommendationId: String)
    
    @Query("DELETE FROM eduwise_recommendations WHERE userId = :userId AND createdDate < :olderThan AND isCompleted = 1")
    suspend fun deleteOldCompletedRecommendations(userId: String, olderThan: Date)
} 