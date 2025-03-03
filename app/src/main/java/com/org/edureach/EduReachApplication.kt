package com.org.edureach

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.org.edureach.data.repository.OfflineRepository

class EduReachApplication : Application() {
    
    private lateinit var sharedPreferences: SharedPreferences
    
    // Offline repository for managing offline content
    lateinit var offlineRepository: OfflineRepository
        private set
    
    // Low bandwidth mode state
    private val _isLowBandwidthMode = MutableLiveData<Boolean>()
    val isLowBandwidthMode: Boolean
        get() = _isLowBandwidthMode.value ?: false
    
    // Offline mode state
    private val _isOfflineMode = MutableLiveData<Boolean>()
    val isOfflineMode: LiveData<Boolean> = _isOfflineMode
    
    override fun onCreate() {
        super.onCreate()
        instance = this
        
        // Initialize shared preferences
        sharedPreferences = getSharedPreferences("edureach_prefs", Context.MODE_PRIVATE)
        
        // Load low bandwidth mode setting
        _isLowBandwidthMode.value = sharedPreferences.getBoolean(KEY_LOW_BANDWIDTH_MODE, false)
        
        // Initialize repositories
        offlineRepository = OfflineRepository(applicationContext)
    }
    
    fun setLowBandwidthMode(enabled: Boolean) {
        _isLowBandwidthMode.value = enabled
        sharedPreferences.edit().putBoolean(KEY_LOW_BANDWIDTH_MODE, enabled).apply()
    }
    
    fun setOfflineMode(enabled: Boolean) {
        _isOfflineMode.value = enabled
    }
    
    companion object {
        private const val KEY_LOW_BANDWIDTH_MODE = "low_bandwidth_mode"
        
        private lateinit var instance: EduReachApplication
        
        fun getInstance(): EduReachApplication {
            return instance
        }
    }
} 