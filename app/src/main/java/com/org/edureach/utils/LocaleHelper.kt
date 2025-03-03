package com.org.edureach.utils

import android.content.Context
import android.content.SharedPreferences
import android.content.res.Configuration
import android.content.res.Resources
import android.os.Build
import android.os.LocaleList
import java.util.Locale

/**
 * Helper class for managing app language settings
 */
object LocaleHelper {
    private const val SELECTED_LANGUAGE = "Locale.Helper.Selected.Language"
    private const val SYSTEM_DEFAULT = "system_default"
    
    // Get available languages with their display names
    fun getAvailableLanguages(): Map<String, String> {
        return mapOf(
            "en" to "English",
            "hi" to "हिन्दी (Hindi)",
            "es" to "Español (Spanish)",
            "fr" to "Français (French)",
            "ar" to "العربية (Arabic)",
            "sw" to "Kiswahili (Swahili)",
            SYSTEM_DEFAULT to "System Default"
        )
    }
    
    // Save selected language
    fun setLocale(context: Context, language: String) {
        persist(context, language)
        updateResources(context, language)
    }
    
    // Get current language
    fun getLanguage(context: Context): String {
        val prefs: SharedPreferences = context.getSharedPreferences("EduReachPrefs", Context.MODE_PRIVATE)
        return prefs.getString(SELECTED_LANGUAGE, SYSTEM_DEFAULT) ?: SYSTEM_DEFAULT
    }
    
    // Initialize with saved language
    fun onAttach(context: Context): Context {
        val lang = getLanguage(context)
        return if (lang == SYSTEM_DEFAULT) {
            context
        } else {
            updateResources(context, lang)
        }
    }
    
    // Persist language preference
    private fun persist(context: Context, language: String) {
        val prefs = context.getSharedPreferences("EduReachPrefs", Context.MODE_PRIVATE)
        prefs.edit().putString(SELECTED_LANGUAGE, language).apply()
    }
    
    // Update app resources with selected language
    private fun updateResources(context: Context, language: String): Context {
        if (language == SYSTEM_DEFAULT) {
            // Use system default
            return context
        }

        val locale = Locale(language)
        Locale.setDefault(locale)
        
        val resources = context.resources
        val config = Configuration(resources.configuration)
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            val localeList = LocaleList(locale)
            LocaleList.setDefault(localeList)
            config.setLocales(localeList)
        } else {
            config.locale = locale
        }
        
        resources.updateConfiguration(config, resources.displayMetrics)
        
        return context.createConfigurationContext(config)
    }
    
    // Check if the language is RTL (Right-to-Left)
    fun isRtl(language: String): Boolean {
        return language == "ar" || language == "fa" || language == "ur"
    }
} 