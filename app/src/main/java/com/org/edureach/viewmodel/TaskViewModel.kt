package com.org.edureach.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.org.edureach.data.TaskRepository
import com.org.edureach.ui.Task
import com.org.edureach.util.NotificationHelper
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class TaskViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = TaskRepository()
    private val notificationHelper = NotificationHelper(application)
    
    private val _tasks = MutableStateFlow<List<Task>>(emptyList())
    val tasks: StateFlow<List<Task>> = _tasks.asStateFlow()
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()
    
    private val _operationSuccessful = MutableStateFlow<Boolean?>(null)
    val operationSuccessful: StateFlow<Boolean?> = _operationSuccessful.asStateFlow()
    
    init {
        loadTasks()
    }
    
    private fun loadTasks() {
        viewModelScope.launch {
            _isLoading.value = true
            repository.getTasks().collect { taskList ->
                _tasks.value = taskList
                _isLoading.value = false
            }
        }
    }
    
    fun addTask(title: String, description: String, dueDate: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            _operationSuccessful.value = null
            
            val task = Task(
                id = "", // Firebase will generate the ID
                title = title,
                description = description,
                dueDate = dueDate,
                isCompleted = false
            )
            
            repository.addTask(task).fold(
                onSuccess = { taskId ->
                    Log.d(TAG, "Task added successfully with ID: $taskId")
                    // Reset error and show success
                    _errorMessage.value = null
                    _operationSuccessful.value = true
                    
                    // Schedule notification for the new task if due date is available
                    if (dueDate.isNotEmpty()) {
                        notificationHelper.scheduleTaskNotification(taskId, task.title, task.dueDate)
                    }
                },
                onFailure = { exception ->
                    Log.e(TAG, "Failed to add task: ${exception.message}", exception)
                    _errorMessage.value = exception.message ?: "Failed to add task. Please try again."
                    _operationSuccessful.value = false
                }
            )
            
            _isLoading.value = false
        }
    }
    
    fun updateTaskCompletion(task: Task) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            
            val updatedTask = task.copy(isCompleted = !task.isCompleted)
            
            repository.updateTask(updatedTask).fold(
                onSuccess = {
                    Log.d(TAG, "Task completion updated successfully: ${task.id}")
                    
                    // Manage notification based on completion status
                    if (updatedTask.isCompleted) {
                        notificationHelper.cancelTaskNotification(task.id)
                    } else if (task.dueDate.isNotEmpty()) {
                        // Reschedule notification if task is marked as incomplete
                        notificationHelper.scheduleTaskNotification(task.id, task.title, task.dueDate)
                    }
                },
                onFailure = { exception ->
                    Log.e(TAG, "Failed to update task completion: ${exception.message}", exception)
                    _errorMessage.value = exception.message ?: "Failed to update task. Please try again."
                }
            )
            
            _isLoading.value = false
        }
    }
    
    fun deleteTask(taskId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            
            repository.deleteTask(taskId).fold(
                onSuccess = {
                    Log.d(TAG, "Task deleted successfully: $taskId")
                    // Cancel any scheduled notifications for this task
                    notificationHelper.cancelTaskNotification(taskId)
                },
                onFailure = { exception ->
                    Log.e(TAG, "Failed to delete task: ${exception.message}", exception)
                    _errorMessage.value = exception.message ?: "Failed to delete task. Please try again."
                }
            )
            
            _isLoading.value = false
        }
    }
    
    /**
     * Clear any error message
     */
    fun clearError() {
        _errorMessage.value = null
    }
    
    /**
     * Reset operation status
     */
    fun resetOperationStatus() {
        _operationSuccessful.value = null
    }
    
    companion object {
        private const val TAG = "TaskViewModel"
    }
} 