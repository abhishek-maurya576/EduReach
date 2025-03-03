package com.org.edureach.viewmodel

import android.app.Application
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
            val task = Task(
                id = "", // Firebase will generate the ID
                title = title,
                description = description,
                dueDate = dueDate,
                isCompleted = false
            )
            repository.addTask(task)
            // Schedule notification for the new task
            notificationHelper.scheduleTaskNotification(task.id, task.title, task.dueDate)
        }
    }
    
    fun updateTaskCompletion(task: Task) {
        viewModelScope.launch {
            val updatedTask = task.copy(isCompleted = !task.isCompleted)
            repository.updateTask(updatedTask)
            
            // Cancel notification if task is completed
            if (updatedTask.isCompleted) {
                notificationHelper.cancelTaskNotification(task.id)
            } else {
                // Reschedule notification if task is marked as incomplete
                notificationHelper.scheduleTaskNotification(task.id, task.title, task.dueDate)
            }
        }
    }
    
    fun deleteTask(taskId: String) {
        viewModelScope.launch {
            repository.deleteTask(taskId)
            // Cancel any scheduled notifications for this task
            notificationHelper.cancelTaskNotification(taskId)
        }
    }
} 