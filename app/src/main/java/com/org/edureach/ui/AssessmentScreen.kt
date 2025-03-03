package com.org.edureach.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.org.edureach.data.Question
import com.org.edureach.network.GeminiApiService
import kotlinx.coroutines.launch

@Composable
fun AssessmentScreen(navController: NavController, lessonId: String) {
    var questions by remember { mutableStateOf<List<Question>>(emptyList()) }
    var currentQuestionIndex by remember { mutableStateOf(0) }
    var selectedAnswer by remember { mutableStateOf<Int?>(null) }
    var isLoading by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(lessonId) {
        isLoading = true
        error = null
        try {
            // Use a hardcoded lesson ID for testing
            questions = GeminiApiService.generateQuestions("lesson1")
        } catch (e: Exception) {
            error = e.message
        } finally {
            isLoading = false
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        if (isLoading) {
            CircularProgressIndicator()
        } else if (error != null) {
            Text(text = error!!, color = MaterialTheme.colorScheme.error)
        } else if (questions.isNotEmpty()) {
            val currentQuestion = questions[currentQuestionIndex]

            Text(text = currentQuestion.text)
            Spacer(modifier = Modifier.height(16.dp))

            currentQuestion.options.forEachIndexed { index, option ->
                Row(verticalAlignment = Alignment.CenterVertically) {
                    RadioButton(
                        selected = selectedAnswer == index,
                        onClick = { selectedAnswer = index }
                    )
                    Text(text = option)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    // TODO: Process answer
                    selectedAnswer?.let { answer ->
                        if (currentQuestionIndex < questions.size - 1) {
                            currentQuestionIndex++
                            selectedAnswer = null // Reset selection for the next question
                        } else {
                            // TODO: Handle assessment completion (e.g., show results)
                            navController.popBackStack()
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = selectedAnswer != null
            ) {
                Text(if (currentQuestionIndex < questions.size - 1) "Next Question" else "Submit")
            }
        } else {
            Text("No questions available for this lesson.")
        }

        Button(
            onClick = { navController.popBackStack() },
            modifier = Modifier.align(Alignment.CenterHorizontally)
        ) {
            Text("Return to Lesson")
        }
    }
}
