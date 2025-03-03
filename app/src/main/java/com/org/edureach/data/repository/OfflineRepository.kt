package com.org.edureach.data.repository

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.asLiveData
import androidx.lifecycle.map
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.org.edureach.data.Lesson
import com.org.edureach.data.Question
import com.org.edureach.data.database.AppDatabase
import com.org.edureach.data.database.CachedLesson
import com.org.edureach.data.database.CachedQuestion
import com.org.edureach.data.database.DownloadedMedia
import com.org.edureach.data.database.UserProgress
import com.org.edureach.data.database.entities.LessonEntity
import com.org.edureach.data.database.entities.ProgressEntity
import com.org.edureach.data.database.entities.QuestionEntity
import com.org.edureach.network.MockApiService
import com.org.edureach.network.SyncWorker
import com.org.edureach.utils.NetworkUtils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.util.UUID
import java.util.concurrent.TimeUnit

/**
 * Repository for managing offline content and synchronization
 */
class OfflineRepository(private val context: Context) {
    private val database = AppDatabase.getDatabase(context)
    private val lessonDao = database.lessonDao()
    private val questionDao = database.questionDao()
    private val mediaDao = database.mediaDao()
    private val progressDao = database.progressDao()
    
    private val mockApiService = MockApiService() // For demo purposes
    
    private val _offlineMode = MutableLiveData(false)
    val offlineMode: LiveData<Boolean> = _offlineMode
    
    private val _downloadProgress = MutableLiveData(0f)
    val downloadProgress: LiveData<Float> = _downloadProgress
    
    private val coroutineScope = CoroutineScope(Dispatchers.IO)
    
    init {
        updateNetworkStatus()
        schedulePeriodicSync()
    }
    
    // Update network status periodically
    private fun updateNetworkStatus() {
        _offlineMode.value = !NetworkUtils.isNetworkAvailable(context)
        
        // Setup periodic network check
        coroutineScope.launch {
            while (true) {
                withContext(Dispatchers.IO) {
                    _offlineMode.postValue(!NetworkUtils.isNetworkAvailable(context))
                    kotlinx.coroutines.delay(30000) // Check every 30 seconds
                }
            }
        }
    }
    
    // Schedule periodic background sync with WorkManager
    private fun schedulePeriodicSync() {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()
            
        val syncRequest = PeriodicWorkRequestBuilder<SyncWorker>(
            1, TimeUnit.HOURS // Sync every hour when online
        )
            .setConstraints(constraints)
            .build()
            
        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            "edu_reach_sync",
            ExistingPeriodicWorkPolicy.KEEP,
            syncRequest
        )
    }
    
    /**
     * Get all lessons, either from cache or API
     */
    fun getAllLessons(): LiveData<List<Lesson>> {
        return if (_offlineMode.value == true) {
            // Get from cache when offline
            lessonDao.getAllLessons().map { lessons ->
                lessons.map { it.toDomainModel() }
            }.asLiveData()
        } else {
            // Get from API when online, but fallback to cache
            val result = MutableLiveData<List<Lesson>>()
            
            coroutineScope.launch {
                try {
                    val networkLessons = mockApiService.getLessons()
                    
                    // Cache the new data
                    for (lesson in networkLessons) {
                        lessonDao.insertLesson(lesson.toCacheModel())
                    }
                    
                    result.postValue(networkLessons)
                } catch (e: Exception) {
                    // Fallback to cache on error
                    val cachedLessons = lessonDao.getAllLessons().map { lessons ->
                        lessons.map { it.toDomainModel() }
                    }.asLiveData().value ?: emptyList()
                    
                    result.postValue(cachedLessons)
                }
            }
            
            result
        }
    }
    
    /**
     * Get lesson by ID
     */
    fun getLessonById(lessonId: String): LiveData<Lesson?> {
        val result = MutableLiveData<Lesson?>()
        
        coroutineScope.launch {
            try {
                val cachedLesson = lessonDao.getLessonById(lessonId)
                
                if (cachedLesson != null) {
                    result.postValue(cachedLesson.toDomainModel())
                } else if (!_offlineMode.value!!) {
                    // Try to fetch from network if not in offline mode
                    val networkLesson = mockApiService.getLessonById(lessonId)
                    lessonDao.insertLesson(networkLesson.toCacheModel())
                    result.postValue(networkLesson)
                } else {
                    result.postValue(null)
                }
            } catch (e: Exception) {
                // Just return null on error
                result.postValue(null)
            }
        }
        
        return result
    }
    
    /**
     * Get questions for a lesson
     */
    fun getQuestionsForLesson(lessonId: String): LiveData<List<Question>> {
        return if (_offlineMode.value == true) {
            // Get from cache when offline
            questionDao.getQuestionsForLesson(lessonId).map { cachedQuestions ->
                cachedQuestions.map { it.toDomainModel() }
            }.asLiveData()
        } else {
            // Try API first, fall back to cache
            val result = MutableLiveData<List<Question>>()
            
            coroutineScope.launch {
                try {
                    val networkQuestions = mockApiService.getQuestionsForLesson(lessonId)
                    
                    // Cache the new questions
                    for (question in networkQuestions) {
                        questionDao.insertQuestion(question.toCacheModel(lessonId))
                    }
                    
                    result.postValue(networkQuestions)
                } catch (e: Exception) {
                    // Fallback to cache on error
                    val cachedQuestions = questionDao.getQuestionsForLesson(lessonId).map { cached ->
                        cached.map { it.toDomainModel() }
                    }.asLiveData().value ?: emptyList()
                    
                    result.postValue(cachedQuestions)
                }
            }
            
            result
        }
    }
    
    /**
     * Download a lesson for offline use
     */
    suspend fun downloadLessonForOffline(lessonId: String): Boolean {
        try {
            // Simulate download progress
            for (i in 1..10) {
                _downloadProgress.postValue(i / 10f)
                kotlinx.coroutines.delay(200)
            }
            
            // In a real app, this would download and store media files
            val lesson = mockApiService.getLessonById(lessonId)
            val questions = mockApiService.getQuestionsForLesson(lessonId)
            
            // Cache lesson and questions
            lessonDao.insertLesson(lesson.toCacheModel())
            for (question in questions) {
                questionDao.insertQuestion(question.toCacheModel(lessonId))
            }
            
            _downloadProgress.postValue(1f)
            return true
        } catch (e: Exception) {
            _downloadProgress.postValue(0f)
            return false
        }
    }
    
    /**
     * Update lesson progress
     */
    suspend fun updateLessonProgress(userId: String, lessonId: String, completed: Boolean) {
        val progressId = UUID.randomUUID().toString()
        val userProgress = UserProgress(
            progressId = progressId,
            userId = userId,
            lessonId = lessonId,
            completed = completed,
            needsSync = true
        )
        
        progressDao.insertProgress(userProgress)
    }
    
    /**
     * Set offline mode
     */
    fun setOfflineMode(offline: Boolean) {
        _offlineMode.postValue(offline)
    }
    
    /**
     * Clear downloaded content
     */
    suspend fun clearDownloadedContent() {
        // In a real app, would also delete downloaded media files
    }
    
    /**
     * Update user progress
     */
    suspend fun updateProgress(userId: String, lessonId: String, completed: Boolean, score: Int? = null) {
        val progressId = UUID.randomUUID().toString()
        val userProgress = UserProgress(
            progressId = progressId,
            userId = userId,
            lessonId = lessonId,
            completed = completed,
            score = score,
            needsSync = true
        )
        
        progressDao.insertProgress(userProgress)
    }
    
    // Extension functions to convert between domain and cache models
    private fun CachedLesson.toDomainModel(): Lesson {
        return Lesson(
            id = this.id,
            title = this.title,
            description = this.description,
            content = this.content,
            videoId = this.videoId,
            isCompleted = this.isCompleted
        )
    }
    
    private fun Lesson.toCacheModel(): CachedLesson {
        return CachedLesson(
            id = this.id,
            title = this.title,
            description = this.description,
            content = this.content,
            videoId = this.videoId,
            isCompleted = this.isCompleted
        )
    }
    
    private fun CachedQuestion.toDomainModel(): Question {
        return Question(
            questionId = this.questionId,
            lessonId = this.lessonId,
            text = this.text,
            options = com.google.gson.Gson().fromJson(this.options, Array<String>::class.java).toList(),
            correctAnswerIndex = this.correctAnswerIndex,
            explanation = this.explanation
        )
    }
    
    private fun Question.toCacheModel(lessonId: String): CachedQuestion {
        return CachedQuestion(
            questionId = this.questionId,
            lessonId = lessonId,
            text = this.text,
            options = com.google.gson.Gson().toJson(this.options),
            correctAnswerIndex = this.correctAnswerIndex,
            explanation = this.explanation
        )
    }
} 