package com.org.edureach.network

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import com.org.edureach.data.User
import retrofit2.http.Body
import retrofit2.http.POST

interface ApiService {
    @POST("users")
    suspend fun saveUser(@Body user: User)
}

object RetrofitClient {
    private const val BASE_URL = "https://your-api-url.com/"
    
    val apiService: ApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }

    suspend fun saveUser(user: User) {
        // Implementation for saving user to backend
        apiService.saveUser(user)
    }
}
