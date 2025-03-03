package com.org.edureach.ui

import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.org.edureach.data.Lesson
import com.org.edureach.network.GeminiApiService
import com.org.edureach.network.RetrofitClient
import kotlinx.coroutines.tasks.await
import androidx.navigation.NavController
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.LaunchedEffect

@Composable
fun LearningPathScreen(navController: NavController) {
    val auth = Firebase.auth
    val firestore = Firebase.firestore
    var userInterests by remember { mutableStateOf(emptyList<String>()) }
    var lessons by remember { mutableStateOf(emptyList<Lesson>()) }
    var isLoading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        try {
            val user = auth.currentUser
            if (user != null) {
                // Get user interests from Firestore
                val snapshot = firestore.collection("users").document(user.uid).get().await()
                @Suppress("UNCHECKED_CAST")
                userInterests = (snapshot.get("interests") as? List<*>)?.filterIsInstance<String>() ?: emptyList()

                // Generate learning path using Gemini
                val generatedContent = generateLearningPathContent(userInterests)
                lessons = parseLessonsFromResponse(generatedContent)
            }
        } catch (e: Exception) {
            error = "Error loading learning path: ${e.message}"
        } finally {
            isLoading = false
        }
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        if (isLoading) {
            CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
        } else if (error != null) {
            Text(text = error!!, color = MaterialTheme.colorScheme.error)
        } else {
            LazyColumn {
                items(lessons) { lesson ->
                    LessonItem(lesson, navController)
                }
            }
        }
    }
}

private suspend fun generateLearningPathContent(interests: List<String>): String {
    return try {
        GeminiApiService.generateLearningPath(interests)
    } catch (e: Exception) {
        "Error generating content: ${e.message}"
    }
}

private fun parseLessonsFromResponse(response: String): List<Lesson> {
    return response.split("\n").mapIndexed { index, line ->
        Lesson(
            id = "lesson-${index + 1}",
            title = line.substringAfter("**").substringBefore("**").trim(),
            description = line.substringAfter(":").trim(),
            content = "Detailed content for ${line.trim()}"
        )
    }
}

@Composable
fun LessonItem(lesson: Lesson, navController: NavController) {
  Card(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
    Column(modifier = Modifier.padding(16.dp)) {
      Text(text = lesson.title)
      Spacer(modifier = Modifier.height(8.dp))
      Button(onClick = { navController.navigate("content/lesson1") }) { // Pass the lesson ID
        Text("Start Lesson")
      }
    }
  }
}
