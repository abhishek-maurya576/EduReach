package com.org.edureach.data.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Entity class representing a lesson stored in the database
 */
@Entity(tableName = "lessons")
data class LessonEntity(
    @PrimaryKey
    val id: String,
    val title: String,
    val description: String,
    val content: String,
    val videoId: String? = null,
    val isCompleted: Boolean = false
) 