package com.org.edureach.data.database

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.util.Date

@Entity(tableName = "cached_lessons")
data class CachedLesson(
    @PrimaryKey val id: String,
    val title: String,
    val description: String,
    val content: String,
    val videoId: String?,
    val lastUpdated: Long = System.currentTimeMillis(),
    val isCompleted: Boolean = false,
    val hasBeenViewed: Boolean = false
)

@Entity(tableName = "cached_questions")
data class CachedQuestion(
    @PrimaryKey val questionId: String,
    val lessonId: String,
    val text: String,
    val options: String, // JSON stringified list
    val correctAnswerIndex: Int,
    val explanation: String,
    val lastUpdated: Long = System.currentTimeMillis()
)

@Entity(tableName = "downloaded_media")
data class DownloadedMedia(
    @PrimaryKey val url: String,
    val lessonId: String,
    val localPath: String,
    val mediaType: String, // VIDEO, IMAGE, AUDIO
    val size: Long,
    val downloadDate: Long = System.currentTimeMillis()
)

@Entity(tableName = "user_progress")
data class UserProgress(
    @PrimaryKey val progressId: String,
    val userId: String,
    val lessonId: String,
    val completed: Boolean,
    val score: Int? = null,
    val lastUpdated: Long = System.currentTimeMillis(),
    val needsSync: Boolean = true // Flag for items that need server synchronization
)

class Converters {
    private val gson = Gson()
    
    @TypeConverter
    fun fromTimestamp(value: Long?): Date? {
        return value?.let { Date(it) }
    }

    @TypeConverter
    fun dateToTimestamp(date: Date?): Long? {
        return date?.time
    }

    @TypeConverter
    fun fromStringList(value: String): List<String> {
        val listType = object : TypeToken<List<String>>() {}.type
        return gson.fromJson(value, listType)
    }

    @TypeConverter
    fun toStringList(list: List<String>): String {
        return gson.toJson(list)
    }
} 