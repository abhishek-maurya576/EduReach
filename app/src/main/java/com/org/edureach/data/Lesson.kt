package com.org.edureach.data

data class Lesson(
    val id: String,
    val title: String,
    val description: String,
    val content: String,
    val videoId: String? = null,
    var isCompleted: Boolean = false
)
