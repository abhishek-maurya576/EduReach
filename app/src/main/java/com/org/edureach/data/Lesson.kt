package com.org.edureach.data

/**
 * Data class representing a lesson in the EduReach application
 */
data class Lesson(
    val id: String = "",
    val title: String = "",
    val description: String = "",
    val content: String = "",
    val videoId: String? = null,
    val imageUrl: String? = null,
    val codeSnippets: List<CodeSnippet> = emptyList(),
    val quizId: String? = null,
    val duration: Int = 0, // in minutes
    val isCompleted: Boolean = false
)

/**
 * Data class representing a code snippet in a lesson
 */
data class CodeSnippet(
    val code: String,
    val description: String,
    val language: String = "python"
)
