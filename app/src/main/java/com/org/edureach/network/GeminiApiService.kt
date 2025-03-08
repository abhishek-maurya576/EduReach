package com.org.edureach.network

import android.util.Log
import com.google.gson.Gson
import com.google.gson.JsonParser
import com.org.edureach.data.Question
import com.org.edureach.viewmodels.ResponseLength
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import kotlinx.serialization.decodeFromString
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.util.concurrent.TimeUnit

object GeminiApiService {
    private val API_KEY = "AIzaSyAKcJKuxaFVigsOErsKtZmPpsIFqGXftKc"
    private const val TAG = "GeminiApiService"
    private const val BASE_URL = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.0-flash:generateContent"
    
    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()
    
    private val gson = Gson()
    
    suspend fun generateStructuredResponse(
        subject: String, 
        topic: String, 
        subTopic: String? = null, 
        question: String? = null,
        responseLength: ResponseLength = ResponseLength.LONG
    ): String {
        return withContext(Dispatchers.IO) {
            try {
                val prompt = buildStructuredPrompt(subject, topic, subTopic, question, responseLength)
                val requestBody = buildRequestBody(prompt)
                val request = buildRequest(requestBody)
                
                val response = client.newCall(request).execute()
                
                if (response.isSuccessful) {
                    val responseBody = response.body?.string() ?: throw Exception("Empty response body")
                    parseResponse(responseBody)
                } else {
                    val errorBody = response.body?.string() ?: "Unknown error"
                    Log.e(TAG, "API request failed: $errorBody")
                    "API request failed with status code ${response.code}: $errorBody"
                }
            } catch (e: Exception) {
                Log.e(TAG, "Failed to generate response: ${e.message}")
                "Sorry, I encountered an error: ${e.message ?: "Unknown error"}"
            }
        }
    }
    
    private fun buildStructuredPrompt(
        subject: String, 
        topic: String, 
        subTopic: String?, 
        question: String?,
        responseLength: ResponseLength
    ): String {
        val promptBuilder = StringBuilder()
        
        // Define the content length guidance based on the selected option
        val lengthGuidance = when (responseLength) {
            ResponseLength.BRIEF -> "Keep the response brief and concise, focusing only on the most essential points."
            ResponseLength.SUMMARY -> "Provide a moderate-length summary with key information."
            ResponseLength.LONG -> "Give a comprehensive, detailed explanation."
        }
        
        promptBuilder.append("Generate educational content for the subject '$subject', topic '$topic'. $lengthGuidance")
        
        if (!subTopic.isNullOrBlank()) {
            promptBuilder.append(" Sub-topic: '$subTopic'.")
        }
        
        if (!question.isNullOrBlank()) {
            promptBuilder.append(" Specifically, answer this question: '$question'.")
        } else {
            promptBuilder.append(" Provide a structured explanation with the following sections:\n")
            
            // Adjust sections based on response length
            if (responseLength == ResponseLength.BRIEF) {
                promptBuilder.append("1. Definition\n")
                promptBuilder.append("2. Key Concepts\n")
            } else if (responseLength == ResponseLength.SUMMARY) {
                promptBuilder.append("1. Definition\n")
                promptBuilder.append("2. Key Concepts\n")
                promptBuilder.append("3. Important Principles\n")
                promptBuilder.append("4. Examples\n")
            } else {
                promptBuilder.append("1. Definition\n")
                promptBuilder.append("2. Key Concepts\n")
                promptBuilder.append("3. Important Principles\n")
                promptBuilder.append("4. Practical Applications\n")
                promptBuilder.append("5. Examples\n")
                promptBuilder.append("6. Common Misconceptions\n")
            }
            
            // Request for visual descriptions if applicable
            promptBuilder.append("\nIf this topic involves diagrams, formulas, or visual aids, please describe them in detail.")
            
            // Add formatting instructions to help with output rendering
            promptBuilder.append("\n\nIMPORTANT: Format your response as follows:")
            promptBuilder.append("\n- Use clear section headings with ## for main sections and ### for subsections")
            promptBuilder.append("\n- Use bold (**text**) for important terms")
            promptBuilder.append("\n- Use bullet points for lists (• item)")
            promptBuilder.append("\n- For mathematical formulas, use plain text descriptions rather than markdown notation")
            promptBuilder.append("\n- Format examples with clear labels and indentation")
            promptBuilder.append("\n- Structure the content to be easily readable on a mobile device")
        }
        
        return promptBuilder.toString()
    }
    
    private fun buildRequestBody(prompt: String): String {
        val jsonRequest = """
        {
            "contents": [
                {
                    "parts": [
                        {
                            "text": "$prompt"
                        }
                    ]
                }
            ],
            "generationConfig": {
                "temperature": 0.7,
                "topK": 1,
                "topP": 0.8,
                "maxOutputTokens": 2048
            }
        }
        """.trimIndent()
        
        return jsonRequest
    }
    
    private fun buildRequest(requestBody: String): Request {
        val mediaType = "application/json".toMediaTypeOrNull()
        val body = requestBody.toRequestBody(mediaType)
        
        return Request.Builder()
            .url("$BASE_URL?key=$API_KEY")
            .post(body)
            .addHeader("Content-Type", "application/json")
            .build()
    }
    
    private fun parseResponse(responseBody: String): String {
        try {
            val jsonElement = JsonParser.parseString(responseBody)
            val candidates = jsonElement.asJsonObject.getAsJsonArray("candidates")
            
            if (candidates.size() > 0) {
                val content = candidates[0].asJsonObject.getAsJsonObject("content")
                val parts = content.getAsJsonArray("parts")
                
                if (parts.size() > 0) {
                    return parts[0].asJsonObject.get("text").asString
                }
            }
            
            return "Could not extract response text from API"
        } catch (e: Exception) {
            Log.e(TAG, "Failed to parse API response: ${e.message}")
            return "Failed to parse response: ${e.message ?: "Unknown error"}"
        }
    }
    
    // Legacy methods using generateStructuredResponse
    suspend fun generateQuestions(lessonId: String): List<Question> {
        val prompt = "Generate 5 multiple choice questions about the lesson with ID $lessonId."
        val response = generateStructuredResponse("Education", "Quiz Generation", "Lesson $lessonId", 
            "Generate 5 multiple choice questions in JSON format. Each question should have: questionId, lessonId, text, options (array), correctAnswerIndex, explanation.")
        
        return try {
            Json.decodeFromString(response)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to parse questions response: ${e.message}")
            emptyList()
        }
    }
    
    suspend fun generateLearningPath(interests: List<String>): String {
        return generateStructuredResponse(
            "Education", 
            "Learning Path", 
            "Personalized Curriculum", 
            "Create a learning path for these interests: ${interests.joinToString()}. Format as a numbered list with bolded titles and descriptions."
        )
    }
    
    suspend fun generateTutorResponse(userMessage: String): String {
        return generateStructuredResponse(
            "Education",
            "AI Tutoring",
            null,
            userMessage
        )
    }
    
    suspend fun generateTopicSummary(topic: String): String {
        return generateStructuredResponse(
            "Education",
            topic,
            "Summary",
            null
        )
    }
    
    suspend fun generateResourceSuggestions(topic: String): String {
        return generateStructuredResponse(
            "Education",
            topic,
            "Learning Resources",
            "Suggest educational resources for learning about this topic. Include books, online courses, websites, video tutorials, and practice exercises."
        )
    }
    
    suspend fun generateConceptExplanation(concept: String): String {
        return generateStructuredResponse(
            "Education",
            concept,
            "Concept Explanation",
            null
        )
    }
}
