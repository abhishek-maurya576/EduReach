package com.org.edureach.data.database.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

/**
 * Entity class representing a question stored in the database
 */
@Entity(
    tableName = "questions",
    foreignKeys = [
        ForeignKey(
            entity = LessonEntity::class,
            parentColumns = ["id"],
            childColumns = ["lessonId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class QuestionEntity(
    @PrimaryKey
    val id: String,
    val lessonId: String,
    val text: String,
    val options: String, // JSON string of options
    val correctOptionIndex: Int,
    val explanation: String
)