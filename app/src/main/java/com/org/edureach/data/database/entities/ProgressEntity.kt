package com.org.edureach.data.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Entity class representing user progress stored in the database
 */
@Entity(tableName = "user_progress")
data class ProgressEntity(
    @PrimaryKey
    val userId: String,
    val lessonId: String,
    val progress: Float,
    val lastUpdated: Long = System.currentTimeMillis()
) 