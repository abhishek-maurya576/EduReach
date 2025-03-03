package com.org.edureach.network

import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.generationConfig
import com.org.edureach.data.Question
import com.org.edureach.BuildConfig
import kotlinx.serialization.json.Json
import kotlinx.serialization.decodeFromString

object GeminiApiService {
    // Use the API key provided by the user
    private val API_KEY = "AIzaSyAKcJKuxaFVigsOErsKtZmPpsIFqGXftKc"
    
    private val generativeModel = GenerativeModel(
        modelName = "gemini-pro",
        apiKey = API_KEY,
        generationConfig = generationConfig {
            temperature = 0.7f
            topK = 1
            topP = 0.7f
        }
    )

    suspend fun generateQuestions(lessonId: String): List<Question> {
        val prompt = """
            Generate 5 multiple choice questions about the lesson with ID $lessonId.
            Return a JSON array of objects, where each object has the following fields:
            questionId (String), lessonId (String), text (String), options (array of Strings), correctAnswerIndex (Int), explanation (String).
        """.trimIndent()

        try {
            val response = generativeModel.generateContent(prompt).text
                ?: throw Exception("Empty response from API")
            return parseGeminiResponse(response)
        } catch (e: Exception) {
            throw Exception("Failed to generate questions: ${e.message ?: "Unknown error"}")
        }
    }

    suspend fun generateLearningPath(interests: List<String>): String {
        val prompt = """
            Generate a learning path for interests: ${interests.joinToString()}.
            Format as numbered list with **title** and :description
        """.trimIndent()

        try {
            val response = generativeModel.generateContent(prompt).text 
                ?: throw Exception("Empty response from API")
            return response
        } catch (e: Exception) {
            throw Exception("Failed to generate learning path: ${e.message ?: "Unknown error"}")
        }
    }

    private fun parseGeminiResponse(response: String): List<Question> {
        return Json.decodeFromString(response)
    }
}
