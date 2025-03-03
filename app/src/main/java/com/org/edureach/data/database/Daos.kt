package com.org.edureach.data.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface LessonDao {
    @Query("SELECT * FROM cached_lessons")
    fun getAllLessons(): Flow<List<CachedLesson>>
    
    @Query("SELECT * FROM cached_lessons WHERE id = :lessonId")
    suspend fun getLessonById(lessonId: String): CachedLesson?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLesson(lesson: CachedLesson)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLessons(lessons: List<CachedLesson>)
    
    @Update
    suspend fun updateLesson(lesson: CachedLesson)
    
    @Query("DELETE FROM cached_lessons WHERE id = :lessonId")
    suspend fun deleteLesson(lessonId: String)
    
    @Query("SELECT COUNT(*) FROM cached_lessons")
    suspend fun getLessonCount(): Int
}

@Dao
interface QuestionDao {
    @Query("SELECT * FROM cached_questions WHERE lessonId = :lessonId")
    fun getQuestionsForLesson(lessonId: String): Flow<List<CachedQuestion>>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertQuestion(question: CachedQuestion)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertQuestions(questions: List<CachedQuestion>)
    
    @Query("DELETE FROM cached_questions WHERE lessonId = :lessonId")
    suspend fun deleteQuestionsForLesson(lessonId: String)
}

@Dao
interface MediaDao {
    @Query("SELECT * FROM downloaded_media WHERE lessonId = :lessonId")
    fun getMediaForLesson(lessonId: String): Flow<List<DownloadedMedia>>
    
    @Query("SELECT * FROM downloaded_media WHERE url = :url")
    suspend fun getMediaByUrl(url: String): DownloadedMedia?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMedia(media: DownloadedMedia)
    
    @Query("DELETE FROM downloaded_media WHERE url = :url")
    suspend fun deleteMedia(url: String)
    
    @Query("SELECT SUM(size) FROM downloaded_media")
    suspend fun getTotalMediaSize(): Long
}

@Dao
interface ProgressDao {
    @Query("SELECT * FROM user_progress WHERE userId = :userId")
    fun getUserProgress(userId: String): Flow<List<UserProgress>>
    
    @Query("SELECT * FROM user_progress WHERE needsSync = 1")
    suspend fun getUnsyncedProgress(): List<UserProgress>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProgress(progress: UserProgress)
    
    @Update
    suspend fun updateProgress(progress: UserProgress)
    
    @Query("UPDATE user_progress SET needsSync = 0 WHERE progressId = :progressId")
    suspend fun markProgressSynced(progressId: String)
} 