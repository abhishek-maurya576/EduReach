package com.org.edureach.ui

import androidx.compose.foundation.background
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

@Composable
fun ProgressScreen(navController: NavController) {
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
                    text = "Progress",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black,
                    modifier = Modifier.padding(start = 8.dp)
                )
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Course progress section
        ProgressItem(
            title = "Your progress",
            label = "75% to complete <Course 1>",
            progress = 0.75f,
            color = Color(0xFF0D6EFD),
            navController = navController
        )
        
        Spacer(modifier = Modifier.height(32.dp))
        
        // Quiz progress section
        ProgressItem(
            title = "Your progress",
            label = "75% to complete <Quiz>",
            progress = 0.75f,
            color = Color(0xFF0D6EFD),
            showButton = true,
            navController = navController
        )
        
        Spacer(modifier = Modifier.height(32.dp))
        
        // Task progress section
        ProgressItem(
            title = "Your progress",
            label = "<Task>",
            progress = 0.3f,
            color = Color(0xFFFFC107),
            navController = navController
        )
    }
}

@Composable
fun ProgressItem(
    title: String,
    label: String,
    progress: Float,
    color: Color,
    showButton: Boolean = false,
    navController: NavController
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        Text(
            text = title,
            fontSize = 12.sp,
            color = Color.Black,
            modifier = Modifier.padding(bottom = 4.dp)
        )
        
        Text(
            text = label,
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium,
            color = Color.Black,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        
        // Custom progress bar
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .background(Color.LightGray.copy(alpha = 0.5f), RoundedCornerShape(4.dp))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(progress)
                    .height(8.dp)
                    .background(color, RoundedCornerShape(4.dp))
            ) {
                // Progress marker dots
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    repeat(5) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .background(Color.White, RoundedCornerShape(4.dp))
                        )
                    }
                }
            }
        }
        
        // Optional Quiz button
        if (showButton) {
            Spacer(modifier = Modifier.height(8.dp))
            
            Button(
                onClick = { navController.navigate("quiz") },
                modifier = Modifier
                    .width(70.dp)
                    .height(36.dp),
                shape = RoundedCornerShape(4.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.Blue
                )
            ) {
                Text(
                    text = "Quiz",
                    fontSize = 14.sp,
                    color = Color.White
                )
            }
        }
    }
}
