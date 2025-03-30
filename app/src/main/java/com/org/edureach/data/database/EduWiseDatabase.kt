package com.org.edureach.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.org.edureach.data.models.EduWiseMessage
import com.org.edureach.data.models.EduWiseSession
import com.org.edureach.data.models.StudyRecommendation

@Database(
    entities = [
        EduWiseSession::class,
        EduWiseMessage::class,
        StudyRecommendation::class
    ],
    version = 1,
    exportSchema = false
)
@TypeConverters(DateConverters::class, EnumConverters::class)
abstract class EduWiseDatabase : RoomDatabase() {
    
    abstract fun eduWiseSessionDao(): EduWiseSessionDao
    abstract fun eduWiseMessageDao(): EduWiseMessageDao
    abstract fun studyRecommendationDao(): StudyRecommendationDao
    
    companion object {
        @Volatile
        private var INSTANCE: EduWiseDatabase? = null
        
        fun getDatabase(context: Context): EduWiseDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    EduWiseDatabase::class.java,
                    "eduwise_database"
                )
                .fallbackToDestructiveMigration()
                .build()
                
                INSTANCE = instance
                instance
            }
        }
    }
} 