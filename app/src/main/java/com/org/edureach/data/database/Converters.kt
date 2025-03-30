package com.org.edureach.data.database

import androidx.room.TypeConverter
import com.org.edureach.data.models.*
import java.util.Date

/**
 * Type converters for handling Date objects in Room
 */
class DateConverters {
    @TypeConverter
    fun fromTimestamp(value: Long?): Date? {
        return value?.let { Date(it) }
    }
    
    @TypeConverter
    fun dateToTimestamp(date: Date?): Long? {
        return date?.time
    }
}

/**
 * Type converters for handling enum values in Room
 */
class EnumConverters {
    @TypeConverter
    fun toLearningStyle(value: String?): LearningStyle? {
        return value?.let { enumValueOf<LearningStyle>(it) }
    }
    
    @TypeConverter
    fun fromLearningStyle(value: LearningStyle?): String? {
        return value?.name
    }
    
    @TypeConverter
    fun toEduWisePersonality(value: String?): EduWisePersonality? {
        return value?.let { enumValueOf<EduWisePersonality>(it) }
    }
    
    @TypeConverter
    fun fromEduWisePersonality(value: EduWisePersonality?): String? {
        return value?.name
    }
    
    @TypeConverter
    fun toEmotionalTone(value: String?): EmotionalTone? {
        return value?.let { enumValueOf<EmotionalTone>(it) }
    }
    
    @TypeConverter
    fun fromEmotionalTone(value: EmotionalTone?): String? {
        return value?.name
    }
    
    @TypeConverter
    fun toRecommendationType(value: String?): RecommendationType? {
        return value?.let { enumValueOf<RecommendationType>(it) }
    }
    
    @TypeConverter
    fun fromRecommendationType(value: RecommendationType?): String? {
        return value?.name
    }
} 