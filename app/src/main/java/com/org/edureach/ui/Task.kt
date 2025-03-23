package com.org.edureach.ui

/**
 * Data class representing a task in the EduReach application
 */
data class Task(
    val id: String = "",
    val title: String = "",
    val description: String = "",
    val dueDate: String = "",
    val isCompleted: Boolean = false
) 