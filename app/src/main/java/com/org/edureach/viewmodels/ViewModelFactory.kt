package com.org.edureach.viewmodels

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

/**
 * Factory for creating ViewModels with dependencies
 */
class ViewModelFactory(private val context: Context) : ViewModelProvider.Factory {
    
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return when {
            modelClass.isAssignableFrom(AITutorFormViewModel::class.java) -> {
                AITutorFormViewModel(context) as T
            }
            modelClass.isAssignableFrom(EduWiseViewModel::class.java) -> {
                EduWiseViewModel(context) as T
            }
            // Add other ViewModels here as needed
            else -> throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
        }
    }
} 