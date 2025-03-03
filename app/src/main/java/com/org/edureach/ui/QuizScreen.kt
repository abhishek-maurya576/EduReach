package com.org.edureach.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.org.edureach.data.Question

@Composable
fun QuizScreen(navController: NavController) {
    // Mock data for demonstration
    val questions = remember {
        listOf(
            Question(
                questionId = "q1",
                lessonId = "l1",
                text = "<Question is here>",
                options = listOf("Option 1", "Option 2", "Option 3", "Option 4"),
                correctAnswerIndex = 0,
                explanation = "Explanation for the correct answer"
            )
        )
    }
    
    var currentQuestionIndex by remember { mutableStateOf(0) }
    var selectedOptionIndex by remember { mutableStateOf<Int?>(null) }
    val currentQuestion = questions[currentQuestionIndex]
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        // Top bar with back button and title
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .background(Color(0xFFDBA84F))
                .padding(horizontal = 16.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = { navController.popBackStack() }
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = Color.Black
                    )
                }
                
                Text(
                    text = "Take a Quiz",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black,
                    modifier = Modifier.padding(start = 8.dp)
                )
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Question card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFFDBA84F)
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                // Question number
                Card(
                    modifier = Modifier.padding(bottom = 16.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFFFFF1E6)
                    )
                ) {
                    Text(
                        text = "Question 01",
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 12.dp),
                        textAlign = TextAlign.Center,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )
                }
                
                // Question text
                Text(
                    text = currentQuestion.text,
                    modifier = Modifier.padding(8.dp),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color.Black
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Options
                currentQuestion.options.forEachIndexed { index, option ->
                    val isSelected = selectedOptionIndex == index
                    
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .clickable { selectedOptionIndex = index },
                        shape = RoundedCornerShape(8.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isSelected) Color(0xFF0D6EFD) else Color(0xFFE6E6E6)
                        )
                    ) {
                        Text(
                            text = option,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            color = if (isSelected) Color.White else Color.Black
                        )
                    }
                }
            }
        }
        
        Spacer(modifier = Modifier.weight(1f))
        
        // Navigation and progress
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Prev button
            Button(
                onClick = {
                    if (currentQuestionIndex > 0) {
                        currentQuestionIndex--
                        selectedOptionIndex = null
                    }
                },
                enabled = currentQuestionIndex > 0,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFDBA84F)
                )
            ) {
                Text("Prev", color = Color.Black)
            }
            
            // Progress indicator
            Text(
                text = "${currentQuestionIndex + 1}/${questions.size}",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )
            
            // Next button
            Button(
                onClick = {
                    if (currentQuestionIndex < questions.size - 1) {
                        currentQuestionIndex++
                        selectedOptionIndex = null
                    }
                },
                enabled = currentQuestionIndex < questions.size - 1,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFDBA84F)
                )
            ) {
                Text("Next", color = Color.Black)
            }
        }
    }
} 