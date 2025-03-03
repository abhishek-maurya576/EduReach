package com.org.edureach.data

import com.google.firebase.Timestamp

data class Progress(
    val userId: String = "",
    val lessonId: String = "",
    var completed: Boolean = false,
    val timestamp: Timestamp = Timestamp.now()
) 