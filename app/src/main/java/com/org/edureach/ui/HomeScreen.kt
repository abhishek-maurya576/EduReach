package com.org.edureach.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.org.edureach.R
import com.org.edureach.ui.components.Avatar
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(navController: NavController) {
    var userName by remember { mutableStateOf("") }
    var userAvatarId by remember { mutableStateOf(1) }
    var isLoading by remember { mutableStateOf(true) }
    var courses by remember { mutableStateOf(listOf<Course>()) }

    LaunchedEffect(Unit) {
        // Fetch user data from Firebase
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        userId?.let { uid ->
            FirebaseFirestore.getInstance().collection("users")
                .document(uid)
                .get()
                .addOnSuccessListener { document ->
                    userName = document.getString("displayName") ?: "User"
                    userAvatarId = document.getLong("avatarId")?.toInt() ?: 1
                    isLoading = false
                }
                .addOnFailureListener {
                    userName = "User"
                    isLoading = false
                }
        }

        courses = listOf(
            Course("Mathematics", "Learn core math concepts", R.drawable.ic_math),
            Course("Quiz Challenge", "Test your knowledge", R.drawable.ic_quiz),
            Course("Tasks & Assignments", "Manage your tasks", R.drawable.ic_task),
            Course("Progress Tracker", "Track your learning", R.drawable.ic_progress),
            Course("AI Tutor", "Get personalized help", R.drawable.ic_ai_tutor)
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "EduReach",
                        style = MaterialTheme.typography.titleLarge.copy(
                            color = Color.Black
                        )
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFFFFE4E1)
                ),
                actions = {
                    IconButton(onClick = { navController.navigate("profile") }) {
                        Avatar(
                            avatarId = userAvatarId,
                            size = 40.dp,
                            onClick = { navController.navigate("profile") }
                        )
                    }
                }
            )
        },
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    icon = { Icon(Icons.Filled.Home, contentDescription = "Home") },
                    label = { Text("Home") },
                    selected = true,
                    onClick = { }
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Filled.List, contentDescription = "Quiz") },
                    label = { Text("Quiz") },
                    selected = false,
                    onClick = { navController.navigate("quiz") }
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Filled.CheckCircle, contentDescription = "Task") },
                    label = { Text("Task") },
                    selected = false,
                    onClick = { navController.navigate("task") }
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Filled.Assessment, contentDescription = "Progress") },
                    label = { Text("Progress") },
                    selected = false,
                    onClick = { navController.navigate("progress") }
                )
            }
        }
    ) { paddingValues ->
        if (isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xFF1A1A1A))
                    .padding(paddingValues)
                    .padding(16.dp)
            ) {
                item {
                    Text(
                        text = "Welcome, $userName!",
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        ),
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                }

                item {
                    Button(
                        onClick = { navController.navigate("ai_tutor") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        )
                    ) {
                        Text(
                            text = "Ask AI Tutor",
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                }

                items(courses) { course ->
                    CourseItem(
                        course = course,
                        onCourseClick = { 
                            when (course.title) {
                                "Mathematics" -> navController.navigate("content")
                                "Quiz Challenge" -> navController.navigate("quiz")
                                "Tasks & Assignments" -> navController.navigate("task")
                                "Progress Tracker" -> navController.navigate("progress")
                                "AI Tutor" -> navController.navigate("ai_tutor")
                            }
                        }
                    )
                }
            }
        }
    }
}

data class Course(
    val title: String,
    val description: String,
    val iconResId: Int
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CourseItem(course: Course, onCourseClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .clickable(onClick = onCourseClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                painter = painterResource(id = course.iconResId),
                contentDescription = course.title,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(32.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(
                    text = course.title,
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = course.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
} 