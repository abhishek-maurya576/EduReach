package com.org.edureach.data

import kotlinx.serialization.Serializable

@Serializable // Add Serializable annotation
data class Question(
    val questionId: String,
    val lessonId: String,
    val text: String,
    val options: List<String>,
    val correctAnswerIndex: Int,
    val explanation: String
)
