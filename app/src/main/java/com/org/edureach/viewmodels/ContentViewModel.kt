package com.org.edureach.viewmodels

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.org.edureach.EduReachApplication
import com.org.edureach.data.Lesson
import com.org.edureach.data.repository.OfflineRepository
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
    
    // UI state holders
    private val _lesson = MutableStateFlow(LessonUiState(isLoading = true))
    val lesson: StateFlow<LessonUiState> = _lesson.asStateFlow()
    
    private val _downloadProgress = MutableStateFlow(0f)
    val downloadProgress: StateFlow<Float> = _downloadProgress.asStateFlow()
    
    private val _isOfflineMode = MutableStateFlow(false)
    val isOfflineMode: StateFlow<Boolean> = _isOfflineMode.asStateFlow()
    
    init {
        // Check network status
        viewModelScope.launch {
            _isOfflineMode.value = !NetworkUtils.isNetworkAvailable(appContext)
            offlineRepository.offlineMode.observeForever { offline ->
                _isOfflineMode.value = offline
            }
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
                    // Fallback to a mock lesson if not found
                    _lesson.value = LessonUiState(data = createMockLesson(lessonId))
                }
            } catch (e: Exception) {
                _lesson.value = LessonUiState(error = e.message)
            }
        }
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
        val currentLesson = _lesson.value.data ?: return
        
        viewModelScope.launch {
            try {
                // Update progress in repository
                offlineRepository.updateLessonProgress(
                    userId = UUID.randomUUID().toString(), // Should be actual user ID in real app
                    lessonId = currentLesson.id,
                    completed = true
                )
                
                // Update UI state
                _lesson.value = _lesson.value.copy(
                    data = currentLesson.copy(isCompleted = true)
                )
            } catch (e: Exception) {
                // Handle error
            }
        }
    }
    
    // For demonstration purposes only
    private fun createMockLesson(lessonId: String): Lesson {
        return Lesson(
            id = lessonId,
            title = "Learning About Solar Energy",
            description = "Understand how solar energy works and its benefits for sustainable development.",
            content = "Solar energy is radiant light and heat from the Sun that is harnessed using a range of technologies such as solar heating, photovoltaics, solar thermal energy, solar architecture, molten salt power plants and artificial photosynthesis.\n\n" +
                "Solar technologies are broadly characterized as either passive solar or active solar depending on how they capture and distribute solar energy or convert it into solar power. Active solar techniques include the use of photovoltaic systems, concentrated solar power, and solar water heating to harness the energy. Passive solar techniques include orienting a building to the Sun, selecting materials with favorable thermal mass or light-dispersing properties, and designing spaces that naturally circulate air.",
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