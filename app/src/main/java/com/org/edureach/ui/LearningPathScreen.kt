package com.org.edureach.ui

import android.content.Context
import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import com.org.edureach.R
import androidx.compose.runtime.DisposableEffect

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LearningPathScreen(navController: NavController) {
    var isLoading by remember { mutableStateOf(false) }
    var userLevel by remember { mutableStateOf(1) } // User's current level in Python
    
    // Get application context
    val context = LocalContext.current
    
    // Track completed lessons
    var completedLessons by remember { mutableStateOf(loadCompletedLessons(context)) }
    
    // Pre-defined Python lessons grouped by levels
    val pythonLevels = remember {
        listOf(
            // Level 1 - Basics
            listOf(
                Lesson(
                    id = "python-basics",
                    title = "Python Basics",
                    description = "Introduction to Python programming language",
                    content = "Learn about Python syntax, variables, and basic data types",
                    isCompleted = completedLessons.contains("python-basics")
                ),
                Lesson(
                    id = "python-control-flow",
                    title = "Control Flow in Python",
                    description = "Master if statements, loops, and control structures",
                    content = "Learn how to control the flow of your Python programs",
                    isCompleted = completedLessons.contains("python-control-flow")
                )
            ),
            // Level 2 - Intermediate
            listOf(
                Lesson(
                    id = "python-functions",
                    title = "Functions and Methods",
                    description = "Define and use functions in Python",
                    content = "Learn how to create reusable code blocks with functions",
                    isCompleted = completedLessons.contains("python-functions")
                ),
                Lesson(
                    id = "python-data-structures",
                    title = "Data Structures",
                    description = "Learn about lists, dictionaries, sets, and tuples",
                    content = "Master Python's built-in data structures",
                    isCompleted = completedLessons.contains("python-data-structures")
                )
            ),
            // Level 3 - Advanced
            listOf(
                Lesson(
                    id = "python-oop",
                    title = "Object-Oriented Programming",
                    description = "Learn OOP concepts in Python",
                    content = "Understand classes, objects, inheritance, and polymorphism",
                    isCompleted = completedLessons.contains("python-oop")
                ),
                Lesson(
                    id = "python-advanced",
                    title = "Advanced Python Topics",
                    description = "Explore file I/O, exceptions, and modules",
                    content = "Take your Python skills to the next level",
                    isCompleted = completedLessons.contains("python-advanced")
                )
            )
        )
    }

    // Effect to refresh lessons when returning to this screen
    DisposableEffect(key1 = navController) {
        val listener = NavController.OnDestinationChangedListener { _, destination, _ ->
            if (destination.route?.startsWith("learning_path") == true) {
                // Reload completed lessons when returning to this screen
                val newCompletedLessons = loadCompletedLessons(context)
                
                // Only update if the completed lessons have changed
                if (newCompletedLessons != completedLessons) {
                    Log.d("LearningPathScreen", "Completed lessons changed: $newCompletedLessons")
                    completedLessons = newCompletedLessons
                    
                    // Recalculate user level
                    val newLevel = calculateUserLevel(completedLessons, pythonLevels)
                    if (newLevel != userLevel) {
                        Log.d("LearningPathScreen", "User level changed from $userLevel to $newLevel")
                        userLevel = newLevel
                    }
                }
            }
        }
        
        navController.addOnDestinationChangedListener(listener)
        
        onDispose {
            navController.removeOnDestinationChangedListener(listener)
        }
    }

    // Effect to load completed lessons
    LaunchedEffect(Unit) {
        try {
            isLoading = true
            // Load completed lessons from shared preferences
            completedLessons = loadCompletedLessons(context)
            
            // Calculate user level based on completed lessons
            val maxLevelUnlocked = calculateUserLevel(completedLessons, pythonLevels)
            userLevel = maxLevelUnlocked
            
            isLoading = false
        } catch (e: Exception) {
            Log.e("LearningPathScreen", "Error loading progress", e)
            isLoading = false
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Python Learning Path") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF306998), // Python blue
                    titleContentColor = Color.White
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFF5F7FA)) // Light background color from reference image
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            // Python logo and intro
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_python),
                    contentDescription = "Python",
                    modifier = Modifier
                        .size(48.dp)
                        .background(Color.White, CircleShape)
                        .padding(8.dp),
                    tint = Color(0xFF306998) // Python blue
                )
                
                Spacer(modifier = Modifier.width(16.dp))
                
                Column {
                    Text(
                        text = "Python Programming",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Master Python from Zero to Hero",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            
            // User progress section
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Text(
                        text = "Your Progress",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    LinearProgressIndicator(
                        progress = 0.15f, // 15% progress for demo
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp),
                        color = Color(0xFF4CAF50),
                        trackColor = Color(0xFFE0E0E0)
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "15% Complete",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        
                        Text(
                            text = "1/6 Lessons",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
            
        if (isLoading) {
            CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
        } else {
                // Learning path levels
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(32.dp)
                ) {
                    items(pythonLevels.size) { levelIndex ->
                        val level = levelIndex + 1
                        val isLevelUnlocked = level <= userLevel
                        
                        Column {
                            // Level header
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Level indicator
                                Box(
                                    modifier = Modifier
                                        .size(40.dp)
                                        .background(
                                            if (isLevelUnlocked) Color(0xFF306998) else Color(0xFFBDBDBD),
                                            CircleShape
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "L$level",
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                
                                Spacer(modifier = Modifier.width(16.dp))
                                
                                Column {
                                    Text(
                                        text = when(level) {
                                            1 -> "Python Fundamentals"
                                            2 -> "Intermediate Concepts"
                                            else -> "Advanced Techniques"
                                        },
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                    
                                    Text(
                                        text = "${pythonLevels[levelIndex].size} lessons",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                
                                Spacer(modifier = Modifier.weight(1f))
                                
                                // Level lock status
                                if (!isLevelUnlocked) {
                                    Icon(
                                        imageVector = Icons.Default.Lock,
                                        contentDescription = "Locked",
                                        tint = Color(0xFFBDBDBD)
                                    )
                                }
                            }
                            
                            // Level lessons
                            pythonLevels[levelIndex].forEachIndexed { lessonIndex, lesson ->
                                // Determine if this lesson should be unlocked
                                val previousLessonCompleted = when {
                                    // First lesson in first level is always unlocked
                                    levelIndex == 0 && lessonIndex == 0 -> true
                                    
                                    // First lesson in level > 1 is unlocked if all lessons in previous level are completed
                                    lessonIndex == 0 && levelIndex > 0 -> {
                                        val previousLevelLessons = pythonLevels[levelIndex - 1].map { it.id }
                                        previousLevelLessons.all { completedLessons.contains(it) }
                                    }
                                    
                                    // Non-first lesson is unlocked if previous lesson in same level is completed
                                    lessonIndex > 0 -> {
                                        val prevLessonId = pythonLevels[levelIndex][lessonIndex - 1].id
                                        completedLessons.contains(prevLessonId)
                                    }
                                    
                                    // Default case - should not happen with proper level/lesson structure
                                    else -> false
                                }
                                
                                val isLessonUnlocked = isLevelUnlocked && previousLessonCompleted
                                
                                // Check if the lesson is completed
                                val isCompleted = lesson.isCompleted || completedLessons.contains(lesson.id)
                                
                                // Create a copy of the lesson with updated completion status
                                val updatedLesson = lesson.copy(isCompleted = isCompleted)
                                
                                LessonItemAdvanced(
                                    lesson = updatedLesson,
                                    isUnlocked = isLessonUnlocked,
                                    index = lessonIndex + 1,
                                    navController = navController
                                )
                                
                                if (lessonIndex < pythonLevels[levelIndex].size - 1) {
                                    // Connector line between lessons
                                    Box(
                                        modifier = Modifier
                                            .padding(start = 20.dp)
                                            .width(2.dp)
                                            .height(24.dp)
                                            .background(
                                                if (isLessonUnlocked) Color(0xFF306998) else Color(0xFFBDBDBD)
                                            )
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Calculate user level based on completed lessons
 */
private fun calculateUserLevel(completedLessons: Set<String>, pythonLevels: List<List<Lesson>>): Int {
    // If no lessons completed, user is at level 1
    if (completedLessons.isEmpty()) return 1
    
    // Check if Level 1 is complete (all lessons in level 1 are completed)
    val level1Ids = pythonLevels.getOrNull(0)?.map { it.id } ?: emptyList()
    val level1Complete = level1Ids.isNotEmpty() && level1Ids.all { completedLessons.contains(it) }
    
    // If any lessons in level 3 are completed, user is at level 3
    val level3Ids = pythonLevels.getOrNull(2)?.map { it.id } ?: emptyList()
    if (level3Ids.any { completedLessons.contains(it) }) return 3
    
    // If Level 1 is complete or any lessons in level 2 are completed, user is at level 2
    val level2Ids = pythonLevels.getOrNull(1)?.map { it.id } ?: emptyList()
    if (level1Complete || level2Ids.any { completedLessons.contains(it) }) return 2
    
    // Otherwise, user is at level 1
    return 1
}

/**
 * Load completed lessons from shared preferences
 */
private fun loadCompletedLessons(context: Context): Set<String> {
    val sharedPrefs = context.getSharedPreferences("edureach_prefs", Context.MODE_PRIVATE)
    val completedLessons = sharedPrefs.getStringSet("completed_lessons", setOf("python-basics")) ?: setOf("python-basics")
    
    // Log the loaded lessons for debugging
    Log.d("LearningPathScreen", "Loaded completed lessons: $completedLessons")
    
    return completedLessons
}

@Composable
fun LessonItemAdvanced(
    lesson: Lesson,
    isUnlocked: Boolean,
    index: Int,
    navController: NavController
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 24.dp)
            .clip(RoundedCornerShape(8.dp))
            .border(
                width = 1.dp,
                color = if (lesson.isCompleted) Color(0xFF4CAF50) 
                        else if (isUnlocked) Color(0xFFE0E0E0) 
                        else Color(0xFFBDBDBD),
                shape = RoundedCornerShape(8.dp)
            ),
        colors = CardDefaults.cardColors(
            containerColor = if (isUnlocked) Color.White else Color(0xFFF5F5F5)
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isUnlocked) 2.dp else 0.dp
        ),
        onClick = { 
            if (isUnlocked) {
                navController.navigate("content/${lesson.id}")
            }
        }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Lesson completion indicator
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(
                        when {
                            lesson.isCompleted -> Color(0xFF4CAF50)
                            isUnlocked -> Color(0xFF306998)
                            else -> Color(0xFFBDBDBD)
                        },
                        CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (lesson.isCompleted) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Completed",
                        tint = Color.White
                    )
                } else {
                    Text(
                        text = "$index",
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = lesson.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = if (isUnlocked) Color.Black else Color(0xFF757575)
                )
                
                Spacer(modifier = Modifier.height(4.dp))
                
                Text(
                    text = lesson.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (isUnlocked) MaterialTheme.colorScheme.onSurfaceVariant else Color(0xFF9E9E9E)
                )
            }
            
            // Lock icon for locked lessons
            if (!isUnlocked) {
                Icon(
                    imageVector = Icons.Default.Lock,
                    contentDescription = "Locked",
                    tint = Color(0xFFBDBDBD)
                )
      }
    }
  }
}
