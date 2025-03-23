package com.org.edureach.viewmodels

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.org.edureach.EduReachApplication
import com.org.edureach.data.Lesson
import com.org.edureach.data.repository.OfflineRepository
import com.org.edureach.data.services.GeminiService
import com.org.edureach.utils.NetworkUtils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.UUID

// State for lesson content
data class LessonUiState(
    val data: Lesson? = null,
    val isLoading: Boolean = false,
    val error: String? = null
)

class ContentViewModel(
    private val appContext: Context,
    private val lessonId: String
) : ViewModel() {
    
    private val offlineRepository: OfflineRepository = EduReachApplication.getInstance().offlineRepository
    private val geminiService = GeminiService()
    
    // UI state holders
    private val _lesson = MutableStateFlow(LessonUiState(isLoading = true))
    val lesson: StateFlow<LessonUiState> = _lesson.asStateFlow()
    
    private val _downloadProgress = MutableStateFlow(0f)
    val downloadProgress: StateFlow<Float> = _downloadProgress.asStateFlow()
    
    private val _isOfflineMode = MutableStateFlow(false)
    val isOfflineMode: StateFlow<Boolean> = _isOfflineMode.asStateFlow()
    
    // For tracking Gemini content status
    private val _isLoadingGeminiContent = MutableStateFlow(false)
    val isLoadingGeminiContent: StateFlow<Boolean> = _isLoadingGeminiContent.asStateFlow()
    
    // W3Schools references
    private val _w3SchoolsReferences = MutableStateFlow<String?>(null)
    val w3SchoolsReferences: StateFlow<String?> = _w3SchoolsReferences.asStateFlow()
    
    // Quiz questions
    private val _quizQuestions = MutableStateFlow<String?>(null)
    val quizQuestions: StateFlow<String?> = _quizQuestions.asStateFlow()
    
    // Track difficulty level
    private val _difficultyLevel = MutableStateFlow("beginner")
    val difficultyLevel: StateFlow<String> = _difficultyLevel.asStateFlow()
    
    // To store completed lessons
    private val _completedLessons = MutableStateFlow<Set<String>>(setOf())
    val completedLessons: StateFlow<Set<String>> = _completedLessons.asStateFlow()
    
    // Track if completion changed
    private val _lessonCompletionChanged = MutableStateFlow(false)
    val lessonCompletionChanged: StateFlow<Boolean> = _lessonCompletionChanged
    
    init {
        // Check network status
        viewModelScope.launch {
            _isOfflineMode.value = !NetworkUtils.isNetworkAvailable(appContext)
            offlineRepository.offlineMode.observeForever { offline ->
                _isOfflineMode.value = offline
            }
            
            // Load completed lessons from storage
            loadCompletedLessons()
            
            // Load the lesson data
            loadLesson(lessonId)
        }
    }
    
    // Load lesson from API or cache
    fun loadLesson(lessonId: String) {
        viewModelScope.launch {
            _lesson.value = LessonUiState(isLoading = true)
            
            try {
                // If offline, try to load from cache first
                if (_isOfflineMode.value) {
                    loadLessonFromOfflineCache()
                    return@launch
                }
                
                // Otherwise, try to load from network
                val lessons = offlineRepository.getAllLessons().value ?: emptyList()
                val lesson = lessons.find { it.id == lessonId }
                
                if (lesson != null) {
                    _lesson.value = LessonUiState(data = lesson)
                } else {
                    // Check if it's a Python lesson
                    if (lessonId.startsWith("python-")) {
                        // Attempt to fetch from Gemini API
                        loadGeminiContent(lessonId)
                    } else {
                        // Fallback to a mock lesson if not found
                        _lesson.value = LessonUiState(data = createMockLesson(lessonId))
                    }
                }
            } catch (e: Exception) {
                _lesson.value = LessonUiState(error = e.message)
            }
        }
    }
    
    // Load content from Gemini API
    private suspend fun loadGeminiContent(lessonId: String) {
        _isLoadingGeminiContent.value = true
        try {
            // Set difficulty level based on lesson ID
            _difficultyLevel.value = when {
                lessonId.contains("basics") || lessonId.contains("control-flow") -> "beginner"
                lessonId.contains("functions") || lessonId.contains("data-structures") -> "intermediate"
                lessonId.contains("oop") || lessonId.contains("advanced") -> "advanced"
                else -> "beginner"
            }
            
            val generatedLesson = geminiService.generateCompletePythonLesson(lessonId)
            
            if (generatedLesson != null) {
                _lesson.value = LessonUiState(data = generatedLesson)
                
                // Preload additional content in the background
                viewModelScope.launch {
                    loadAdditionalContent(lessonId, generatedLesson.title)
                }
                
                // Instead of saving to repository, just update UI
                // offlineRepository.saveLesson(generatedLesson)
            } else {
                // Fallback to mock lesson if Gemini fails
                _lesson.value = LessonUiState(data = createMockLesson(lessonId))
            }
        } catch (e: Exception) {
            _lesson.value = LessonUiState(error = "Failed to generate content: ${e.message}")
        } finally {
            _isLoadingGeminiContent.value = false
        }
    }
    
    // Load additional content like W3Schools references and quiz questions
    private suspend fun loadAdditionalContent(lessonId: String, topic: String) {
        try {
            // Fetch W3Schools references
            _w3SchoolsReferences.value = geminiService.fetchW3SchoolsReferences(topic)
            
            // Fetch quiz questions
            _quizQuestions.value = geminiService.fetchPythonQuizQuestions(topic)
        } catch (e: Exception) {
            Log.e("ContentViewModel", "Error loading additional content", e)
        }
    }
    
    // Load code examples for a specific topic with appropriate difficulty
    suspend fun loadPythonCodeExamples(topic: String): String {
        return try {
            val result = geminiService.fetchPythonCodeExamples(topic, difficultyLevel.value)
            if (result.isNullOrBlank()) {
                "No code examples available for this topic."
            } else {
                result
            }
        } catch (e: Exception) {
            Log.e("ContentViewModel", "Error loading code examples", e)
            "Unable to load code examples: ${e.message ?: "Unknown error"}"
        }
    }
    
    // Load practice exercises for a specific topic with appropriate difficulty
    suspend fun loadPythonExercises(topic: String): String {
        return try {
            val result = geminiService.fetchPythonExercises(topic, difficultyLevel.value)
            if (result.isNullOrBlank()) {
                "No exercises available for this topic."
            } else {
                result
            }
        } catch (e: Exception) {
            Log.e("ContentViewModel", "Error loading exercises", e)
            "Unable to load exercises: ${e.message ?: "Unknown error"}"
        }
    }
    
    // Get W3Schools references
    fun getW3SchoolsReferences(): String {
        return _w3SchoolsReferences.value ?: "Loading W3Schools references..."
    }
    
    // Get quiz questions
    fun getQuizQuestions(): String {
        return _quizQuestions.value ?: "Loading quiz questions..."
    }
    
    // Explicitly load from offline cache
    fun loadLessonFromOfflineCache() {
        viewModelScope.launch {
            try {
                val lessons = offlineRepository.getAllLessons().value ?: emptyList()
                val lesson = lessons.find { it.id == lessonId }
                
                if (lesson != null) {
                    _lesson.value = LessonUiState(data = lesson)
                } else {
                    _lesson.value = LessonUiState(error = "Lesson not available offline")
                }
            } catch (e: Exception) {
                _lesson.value = LessonUiState(error = "Failed to load offline lesson: ${e.message}")
            }
        }
    }
    
    // Download lesson for offline use
    suspend fun downloadLessonForOffline() {
        if (_lesson.value.data == null) return
        
        try {
            // Observe download progress
            offlineRepository.downloadProgress.observeForever { progress ->
                _downloadProgress.value = progress
            }
            
            // Start download
            val success = offlineRepository.downloadLessonForOffline(lessonId)
            
            if (!success) {
                // Handle download failure
            }
        } catch (e: Exception) {
            // Handle error
        }
    }
    
    // Mark lesson as completed
    fun markLessonAsCompleted() {
        viewModelScope.launch {
            _lesson.value.data?.let { lessonData ->
                // Create a copy of the lesson with isCompleted set to true
                val updatedLesson = lessonData.copy(isCompleted = true)
                
                // Generate a mock user ID - in a real app, this would come from authentication
                val userId = UUID.randomUUID().toString()
                
                // Log the completion action
                Log.d("ContentViewModel", "Marking lesson ${updatedLesson.id} as completed")
                
                // Update repositories
                offlineRepository.updateLessonProgress(
                    userId = userId,
                    lessonId = updatedLesson.id,
                    completed = true
                )
                
                // Add to completed lessons
                val updatedCompletedLessons = _completedLessons.value.toMutableSet()
                updatedCompletedLessons.add(updatedLesson.id)
                _completedLessons.value = updatedCompletedLessons
                
                // Save to storage
                saveCompletedLessons(_completedLessons.value)
                Log.d("ContentViewModel", "Updated completed lessons: ${_completedLessons.value}")
                
                // Update the API if we're online
                if (!_isOfflineMode.value) {
                    try {
                        geminiService.updateLessonProgress(userId, updatedLesson.id, true)
                    } catch (e: Exception) {
                        // If API update fails, we still have it in offline repository
                        Log.e("ContentViewModel", "Failed to update progress in API: ${e.message}")
                    }
                }
                
                // Update the UI state with the updated lesson
                _lesson.value = _lesson.value.copy(
                    data = updatedLesson
                )
                
                // Signal that completion has changed
                _lessonCompletionChanged.value = true
            }
        }
    }
    
    // Save completed lessons to storage
    private fun saveCompletedLessons(completedLessons: Set<String>) {
        val sharedPrefs = appContext.getSharedPreferences("edureach_prefs", Context.MODE_PRIVATE)
        val currentLessons = sharedPrefs.getStringSet("completed_lessons", setOf()) ?: setOf()
        
        // Merge current and new completed lessons to ensure we don't lose any
        val mergedLessons = currentLessons.toMutableSet().apply {
            addAll(completedLessons)
        }
        
        // Save the merged set
        sharedPrefs.edit().putStringSet("completed_lessons", mergedLessons).apply()
        Log.d("ContentViewModel", "Saved completed lessons to preferences: $mergedLessons")
    }
    
    // Load completed lessons from storage
    private fun loadCompletedLessons() {
        val sharedPrefs = appContext.getSharedPreferences("edureach_prefs", Context.MODE_PRIVATE)
        val savedLessons = sharedPrefs.getStringSet("completed_lessons", setOf()) ?: setOf()
        _completedLessons.value = savedLessons
    }
    
    // Get completed lessons
    fun getCompletedLessons(): Set<String> {
        return _completedLessons.value
    }
    
    // Reset the completion change flag
    fun resetCompletionChangedFlag() {
        _lessonCompletionChanged.value = false
    }
    
    // For demonstration purposes only
    private fun createMockLesson(lessonId: String): Lesson {
        return Lesson(
            id = lessonId,
            title = "Python Programming Basics",
            description = "Understand Python basic syntax and explore fundamental programming concepts.",
            content = "Python is a high-level, interpreted programming language that is known for its readability and simplicity. It's an excellent language for beginners due to its clear syntax and extensive libraries.\n\n" +
                "Python's philosophy emphasizes code readability with its use of significant whitespace. Its language constructs and object-oriented approach aim to help programmers write clear, logical code for small and large-scale projects.\n\n" +
                "Python is dynamically typed and garbage-collected. It supports multiple programming paradigms, including structured, object-oriented, and functional programming. It is often described as a 'batteries included' language due to its comprehensive standard library.",
            videoId = "dQw4w9WgXcQ",
            isCompleted = false
        )
    }
}

/**
 * Factory for creating ContentViewModel with parameters
 */
class ContentViewModelFactory(
    private val appContext: Context,
    private val lessonId: String
) : ViewModelProvider.Factory {
    
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ContentViewModel::class.java)) {
            return ContentViewModel(appContext, lessonId) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
} 