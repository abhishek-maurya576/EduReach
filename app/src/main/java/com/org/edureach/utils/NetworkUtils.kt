package com.org.edureach.utils

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build

/**
 * Utility class for network-related operations
 */
object NetworkUtils {
    
    /**
     * Check if network is available
     */
    fun isNetworkAvailable(context: Context): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val network = connectivityManager.activeNetwork ?: return false
            val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
            
            return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
        } else {
            @Suppress("DEPRECATION")
            val networkInfo = connectivityManager.activeNetworkInfo
            @Suppress("DEPRECATION")
            return networkInfo != null && networkInfo.isConnected
        }
    }
    
    /**
     * Check if the current connection is metered (e.g., mobile data)
     */
    fun isMeteredConnection(context: Context): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        return connectivityManager.isActiveNetworkMetered
    }
    
    // Recommend content download based on network conditions
    fun shouldDownloadContent(context: Context, contentSizeMB: Int): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val networkCapabilities = connectivityManager.activeNetwork ?: return false
        val actNw = connectivityManager.getNetworkCapabilities(networkCapabilities) ?: return false
        
        // Always allow on WiFi or Ethernet
        if (actNw.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) || 
            actNw.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET)) {
            return true
        }
        
        // On cellular, be more conservative
        return if (actNw.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)) {
            // Only recommend small content (<5MB) on cellular
            contentSizeMB < 5
        } else {
            false
        }
    }
} 