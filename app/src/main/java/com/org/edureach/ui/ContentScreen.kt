package com.org.edureach.ui

import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.draw.clip
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.outlined.CloudOff
import androidx.compose.material.icons.outlined.Done
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import coil.request.CachePolicy
import coil.request.ImageRequest
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.org.edureach.EduReachApplication
import com.org.edureach.R
import com.org.edureach.data.Lesson
import com.org.edureach.viewmodels.ContentViewModel
import com.org.edureach.viewmodels.ContentViewModelFactory
import kotlinx.coroutines.launch
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.graphics.Color
import com.org.edureach.BuildConfig
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.foundation.BorderStroke
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Quiz
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import android.widget.Toast
import android.util.Log

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ContentScreen(
    lessonId: String, 
    navController: NavController,
    viewModel: ContentViewModel = viewModel(
        factory = ContentViewModelFactory(LocalContext.current.applicationContext, lessonId)
    )
) {
    val context = LocalContext.current
    val app = EduReachApplication.getInstance()
    val isLowBandwidthMode = app.isLowBandwidthMode
    
    val lessonState by viewModel.lesson.collectAsState()
    val downloadProgress by viewModel.downloadProgress.collectAsState()
    val isOfflineMode by viewModel.isOfflineMode.collectAsState()
    val isLoadingGeminiContent by viewModel.isLoadingGeminiContent.collectAsState()
    
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()
    
    val scrollState = rememberScrollState()
    
    // Check if Gemini API key is configured
    val geminiApiKey = BuildConfig.GEMINI_API_KEY
    var showApiKeyDialog by remember { mutableStateOf(geminiApiKey.isEmpty() || geminiApiKey == "YOUR_GEMINI_API_KEY_HERE") }
    
    LaunchedEffect(lessonId) {
        viewModel.loadLesson(lessonId)
    }
    
    // Show Gemini API Key setup dialog if needed
    if (showApiKeyDialog && lessonId.startsWith("python-")) {
        GeminiKeySetupDialog(
            onDismiss = { showApiKeyDialog = false }
        )
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = stringResource(R.string.lesson_title)) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.action_back)
                        )
                    }
                },
                actions = {
                    // Download for offline button
                    if (!isOfflineMode && lessonState.data != null) {
                        IconButton(
                            onClick = {
                                coroutineScope.launch {
                                    viewModel.downloadLessonForOffline()
                                    snackbarHostState.showSnackbar(
                                        message = context.getString(R.string.lesson_download_success)
                                    )
                                }
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Download,
                                contentDescription = stringResource(R.string.lesson_download)
                            )
                        }
                    }
                    
                    // Add test Gemini button
                    if (lessonId.startsWith("python-")) {
                        IconButton(
                            onClick = {
                                context.startActivity(Intent(context, TestGeminiActivity::class.java))
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Filled.BugReport,
                                contentDescription = "Test Gemini API"
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        
        // Main content
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(scrollState)
        ) {
            // Offline indicator
            if (isOfflineMode) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .padding(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Outlined.CloudOff,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = stringResource(R.string.offline_mode_active),
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(start = 8.dp)
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
            }
            
            // Show loading indicator when fetching content from Gemini
            if (isLoadingGeminiContent) {
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        CircularProgressIndicator(
                            color = Color(0xFF306998)
                        )
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        Text(
                            text = "Generating Python content...",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }
            
            // Handle different states
            when {
                lessonState.isLoading && !isLoadingGeminiContent -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
                lessonState.error != null -> {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Warning,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(48.dp)
                        )
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        Text(
                            text = lessonState.error ?: stringResource(R.string.error_loading),
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodyLarge
                        )
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        if (!isOfflineMode) {
                            Button(
                                onClick = { viewModel.loadLessonFromOfflineCache() }
                            ) {
                                Text(stringResource(R.string.error_try_offline))
                            }
                        }
                        
                        if (lessonState.error?.contains("Gemini") == true ||
                            lessonState.error?.contains("API key") == true) {
                            Spacer(modifier = Modifier.height(8.dp))
                            
                            Button(
                                onClick = { showApiKeyDialog = true }
                            ) {
                                Text("Configure Gemini API Key")
                            }
                        }
                    }
                }
                lessonState.data != null -> {
                    val lesson = lessonState.data!!
                    
                    // Special handling for Python lessons
                    if (lesson.id.startsWith("python-")) {
                        PythonLessonContent(lesson, navController, viewModel)
                    } else {
                        // Regular lesson content
                        Text(
                            text = lesson.title,
                            style = MaterialTheme.typography.headlineMedium
                        )
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        // Display content differently based on bandwidth mode
                        if (!isLowBandwidthMode && !lesson.videoId.isNullOrEmpty()) {
                            // Video placeholder - in a real app, you'd use a video player here
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(200.dp)
                            ) {
                                Box(
                                    modifier = Modifier.fillMaxSize(),
                                    contentAlignment = Alignment.Center
                                ) {
                                    // In low-bandwidth mode, we would not load the video
                                    // But for demonstration, let's show a placeholder
                                    Text(text = "Video: ${lesson.videoId}")
                                }
                            }
                            
                            Spacer(modifier = Modifier.height(16.dp))
                        }
                        
                        // In a real app, you would have multiple images in the lesson content
                        // Here we're just simulating one for demonstration
                        if (!isLowBandwidthMode) {
                            // This is a placeholder for an image that would be part of the lesson
                            AsyncImage(
                                model = ImageRequest.Builder(LocalContext.current)
                                    .data("https://github.com/abhishek-maurya576/portfolio/blob/main/images/python.png")
                                    .crossfade(true)
                                    .memoryCachePolicy(CachePolicy.ENABLED)
                                    .diskCachePolicy(CachePolicy.ENABLED)
                                    .build(),
                                contentDescription = null,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(200.dp),
                                error = painterResource(id = R.drawable.placeholder_image)
                            )
                            
                            Spacer(modifier = Modifier.height(16.dp))
                        }
                        
                        // Lesson content
                        Text(
                            text = stringResource(R.string.lesson_objective),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.Black
                        )
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        Text(
                            text = lesson.description,
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.Black
                        )
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        Text(
                            text = lesson.content,
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.Black
                        )
                        
                        Spacer(modifier = Modifier.height(24.dp))
                        
                        // Buttons for actions
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Button(
                                onClick = { 
                                    viewModel.markLessonAsCompleted()
                                    coroutineScope.launch {
                                        snackbarHostState.showSnackbar(
                                            message = context.getString(R.string.lesson_completed)
                                        )
                                    }
                                },
                                enabled = !lesson.isCompleted,
                                modifier = Modifier.weight(1f)
                            ) {
                                if (lesson.isCompleted) {
                                    Icon(
                                        imageVector = Icons.Outlined.Done,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                }
                                Text(
                                    text = if (lesson.isCompleted) 
                                        stringResource(R.string.lesson_completed)
                                    else 
                                        stringResource(R.string.lesson_complete)
                                )
                            }
                            
                            Spacer(modifier = Modifier.width(8.dp))
                            
                            Button(
                                onClick = { navController.navigate("assessment/${lesson.id}") },
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(stringResource(R.string.lesson_take_quiz))
                            }
                        }
                        
                        // Display download progress if downloading
                        if (downloadProgress > 0f && downloadProgress < 1f) {
                            Spacer(modifier = Modifier.height(16.dp))
                            
                            Text(
                                text = "Downloading: ${(downloadProgress * 100).toInt()}%",
                                style = MaterialTheme.typography.bodySmall
                            )
                            
                            Spacer(modifier = Modifier.height(8.dp))
                            
                            LinearProgressIndicator(
                                progress = { downloadProgress },
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun PythonLessonContent(
    lesson: Lesson, 
    navController: NavController,
    viewModel: ContentViewModel
) {
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        // Python Lesson Header with Python logo
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp)),
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFF306998) // Python blue
            ),
            elevation = CardDefaults.cardElevation(
                defaultElevation = 4.dp
            )
        ) {
            Column(
                modifier = Modifier.padding(20.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_python),
                        contentDescription = "Python",
                        modifier = Modifier
                            .size(48.dp)
                            .background(Color.White, CircleShape)
                            .padding(8.dp),
                        tint = Color(0xFF306998)
                    )
                    
                    Spacer(modifier = Modifier.width(16.dp))
                    
                    Column {
                        Text(
                            text = lesson.title,
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        
                        Text(
                            text = "Estimated time: ${lesson.duration.takeIf { it > 0 } ?: 15} min",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.White.copy(alpha = 0.8f)
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Text(
                    text = lesson.description,
                    style = MaterialTheme.typography.bodyLarge,
                    color = Color.White
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                LinearProgressIndicator(
                    progress = if (lesson.isCompleted) 1f else 0.3f,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp)),
                    color = Color.White,
                    trackColor = Color.White.copy(alpha = 0.3f)
                )
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Learning objectives
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = Color.White
            ),
            elevation = CardDefaults.cardElevation(
                defaultElevation = 2.dp
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "Learning Objectives",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // Lesson content
                when (lesson.id) {
                    "python-basics" -> PythonBasicsContent(viewModel)
                    "python-control-flow" -> PythonControlFlowContent(viewModel)
                    "python-functions" -> PythonFunctionsContent(viewModel)
                    "python-data-structures" -> PythonDataStructuresContent(viewModel)
                    "python-oop" -> PythonOOPContent(viewModel)
                    "python-advanced" -> PythonAdvancedContent(viewModel)
                    else -> Text(text = lesson.content)
                }
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // W3Schools Reference Section
        W3SchoolsReferenceSection(viewModel)
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Quiz Section
        QuizSection(viewModel)
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Next/Complete button
        CompleteLessonButton(viewModel, navController)
    }
}

@Composable
fun CompleteLessonButton(viewModel: ContentViewModel, navController: NavController) {
    val context = LocalContext.current
    
    Box(modifier = Modifier.fillMaxWidth()) {
        Button(
            onClick = { 
                // Mark lesson as completed
                viewModel.markLessonAsCompleted()
                
                // Show a more descriptive success message
                val lessonData = viewModel.lesson.value.data
                val lessonType = when {
                    lessonData?.id?.contains("basics") == true -> "Python Basics"
                    lessonData?.id?.contains("control-flow") == true -> "Control Flow"
                    lessonData?.id?.contains("functions") == true -> "Functions"
                    lessonData?.id?.contains("data-structures") == true -> "Data Structures"
                    lessonData?.id?.contains("oop") == true -> "OOP"
                    lessonData?.id?.contains("advanced") == true -> "Advanced Python"
                    else -> "Lesson"
                }
                
                Toast.makeText(
                    context,
                    "$lessonType completed! Progress saved and next lesson unlocked.",
                    Toast.LENGTH_LONG
                ).show()
                
                Log.d("ContentScreen", "Lesson ${lessonData?.id} marked as completed")
                
                // Navigate back
                navController.popBackStack()
            },
            modifier = Modifier.align(Alignment.CenterEnd),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF306998)
            )
        ) {
            Text("Complete Lesson")
            Spacer(modifier = Modifier.width(8.dp))
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = "Complete"
            )
        }
    }
}

@Composable
fun W3SchoolsReferenceSection(viewModel: ContentViewModel) {
    var expanded by remember { mutableStateOf(false) }
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFF8F8F8)
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 1.dp
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { expanded = !expanded },
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_task),
                        contentDescription = "W3Schools",
                        tint = Color(0xFF4CAF50)
                    )
                    
                    Spacer(modifier = Modifier.width(8.dp))
                    
                    Text(
                        text = "W3Schools Reference",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
                
                Icon(
                    imageVector = if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = if (expanded) "Collapse" else "Expand"
                )
            }
            
            if (expanded) {
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    text = viewModel.getW3SchoolsReferences(),
                    style = MaterialTheme.typography.bodyMedium
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                OutlinedButton(
                    onClick = { /* Open W3Schools in browser */ },
                    modifier = Modifier.align(Alignment.End),
                    border = BorderStroke(1.dp, Color(0xFF4CAF50))
                ) {
                    Text(
                        text = "View on W3Schools",
                        color = Color(0xFF4CAF50)
                    )
                }
            }
        }
    }
}

@Composable
fun QuizSection(viewModel: ContentViewModel) {
    var expanded by remember { mutableStateOf(false) }
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFF0F7FF)
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 1.dp
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { expanded = !expanded },
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_quiz_icon),
                        contentDescription = "Quiz",
                        tint = Color(0xFF2196F3)
                    )
                    
                    Spacer(modifier = Modifier.width(8.dp))
                    
                    Text(
                        text = "Knowledge Check",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
                
                Icon(
                    imageVector = if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = if (expanded) "Collapse" else "Expand"
                )
            }
            
            if (expanded) {
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    text = viewModel.getQuizQuestions(),
                    style = MaterialTheme.typography.bodyMedium
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Button(
                    onClick = { /* Take full quiz */ },
                    modifier = Modifier.align(Alignment.End),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF2196F3)
                    )
                ) {
                    Text("Take Full Quiz")
                }
            }
        }
    }
}

@Composable
fun PythonAdvancedContent(viewModel: ContentViewModel) {
    Column {
        ContentSection(
            title = "Advanced Python Techniques",
            content = "This section covers advanced Python techniques including file handling, modules, and exception handling."
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        DynamicCodeExamplesSection(viewModel, "Python file handling and modules")
        
        Spacer(modifier = Modifier.height(24.dp))
        
        DynamicExercisesSection(viewModel, "Python exception handling and advanced features")
    }
}

@Composable
fun LearningObjectiveItem(text: String) {
    Row(
        modifier = Modifier.padding(vertical = 4.dp),
        verticalAlignment = Alignment.Top
    ) {
        Box(
            modifier = Modifier
                .size(24.dp)
                .background(Color(0xFF306998), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(16.dp)
            )
        }
        
        Spacer(modifier = Modifier.width(12.dp))
        
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

@Composable
fun PythonBasicsContent(viewModel: ContentViewModel) {
    Column {
        // Syntax section
        ContentSection(
            title = "Python Syntax Basics",
            content = "Python syntax is designed to be readable and simple. Unlike other programming languages, Python uses indentation to define code blocks instead of braces."
        )
        
        // Code example
        PythonCodeSnippet(
            code = """# This is a comment
print("Hello, World!")  # This prints a message
""",
            description = "Your first Python program"
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Variables section
        ContentSection(
            title = "Variables and Data Types",
            content = "Variables are used to store data values. Python has no command for declaring a variable. A variable is created when you first assign a value to it."
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Add dynamic code examples section
        DynamicCodeExamplesSection(viewModel, "Python variables and data types")
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Math operations section
        ContentSection(
            title = "Basic Math Operations",
            content = "Python supports all the standard mathematical operations you would expect."
        )
        
        // Code example
        PythonCodeSnippet(
            code = """# Math operations
a = 10
b = 5

print(a + b)  # Addition: 15
print(a - b)  # Subtraction: 5
print(a * b)  # Multiplication: 50
print(a / b)  # Division: 2.0
print(a % b)  # Modulus: 0
print(a ** b) # Exponentiation: 100000
""",
            description = "Basic arithmetic in Python"
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Dynamic exercises 
        DynamicExercisesSection(viewModel, "Python variables and basic operations")
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Interactive quiz
        ContentSection(
            title = "Quick Check",
            content = "Let's test your understanding with a quick question."
        )
        
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFFF5F5F5)
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "Which of the following is a valid variable name in Python?",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )
                
                Spacer(modifier = Modifier.height(12.dp))
                
                QuizOption("1var = 10", false)
                QuizOption("my_var = 10", true)
                QuizOption("my-var = 10", false)
                QuizOption("class = 10", false)
            }
        }
    }
}

@Composable
fun ContentSection(title: String, content: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold
    )
    
    Spacer(modifier = Modifier.height(8.dp))
    
    Text(
        text = content,
        style = MaterialTheme.typography.bodyMedium
    )
    
    Spacer(modifier = Modifier.height(16.dp))
}

@Composable
fun QuizOption(text: String, isCorrect: Boolean) {
    var isSelected by remember { mutableStateOf(false) }
    var showResult by remember { mutableStateOf(false) }
    
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(4.dp))
            .background(
                when {
                    showResult && isSelected && isCorrect -> Color(0xFFE8F5E9)
                    showResult && isSelected && !isCorrect -> Color(0xFFFFEBEE)
                    isSelected -> Color(0xFFE3F2FD)
                    else -> Color.White
                }
            )
            .border(
                width = 1.dp,
                color = when {
                    showResult && isSelected && isCorrect -> Color(0xFF4CAF50)
                    showResult && isSelected && !isCorrect -> Color(0xFFF44336)
                    isSelected -> Color(0xFF2196F3)
                    else -> Color(0xFFE0E0E0)
                },
                shape = RoundedCornerShape(4.dp)
            )
            .clickable { 
                isSelected = !isSelected
                if (isSelected) {
                    showResult = true
                }
            }
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(
            selected = isSelected,
            onClick = { 
                isSelected = !isSelected
                if (isSelected) {
                    showResult = true
                }
            },
            colors = RadioButtonDefaults.colors(
                selectedColor = when {
                    showResult && isCorrect -> Color(0xFF4CAF50)
                    showResult && !isCorrect -> Color(0xFFF44336)
                    else -> Color(0xFF2196F3)
                }
            )
        )
        
        Spacer(modifier = Modifier.width(8.dp))
        
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium
        )
        
        if (showResult && isSelected) {
            Spacer(modifier = Modifier.weight(1f))
            
            Icon(
                imageVector = if (isCorrect) Icons.Default.Check else Icons.Filled.Warning,
                contentDescription = if (isCorrect) "Correct" else "Incorrect",
                tint = if (isCorrect) Color(0xFF4CAF50) else Color(0xFFF44336)
            )
        }
    }
    
    Spacer(modifier = Modifier.height(8.dp))
}

@Composable
fun PythonCodeSnippet(
    code: String, 
    description: String,
    onRunClick: () -> Unit = {},
    onCopyClick: () -> Unit = {}
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(Color(0xFFF5F5F5))
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = description,
                style = MaterialTheme.typography.labelMedium,
                color = Color(0xFF757575)
            )
            
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onRunClick,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_python),
                        contentDescription = "Run",
                        tint = Color(0xFF306998),
                        modifier = Modifier.size(16.dp)
                    )
                }
                
                IconButton(
                    onClick = onCopyClick,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_task),
                        contentDescription = "Copy",
                        tint = Color(0xFF757575),
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
        
        Text(
            text = code,
            style = MaterialTheme.typography.bodyMedium,
            fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
            color = Color(0xFF000000)
        )
    }
}

@Composable
fun DynamicCodeExamplesSection(
    viewModel: ContentViewModel,
    topic: String
) {
    var codeExamples by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    
    LaunchedEffect(topic) {
        isLoading = true
        try {
            codeExamples = viewModel.loadPythonCodeExamples(topic)
            error = null
        } catch (e: Exception) {
            error = e.message
        } finally {
            isLoading = false
        }
    }
    
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = "Code Examples",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(
                    color = Color(0xFF306998)
                )
            }
        } else if (error != null) {
            Text(
                text = "Unable to load code examples: $error",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Red
            )
        } else if (codeExamples != null) {
            PythonCodeSnippet(
                code = codeExamples!!,
                description = "Generated examples for $topic",
                onCopyClick = {
                    // Copy to clipboard action
                }
            )
        }
    }
}

@Composable
fun DynamicExercisesSection(
    viewModel: ContentViewModel,
    topic: String
) {
    var exercises by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    
    LaunchedEffect(topic) {
        isLoading = true
        try {
            exercises = viewModel.loadPythonExercises(topic)
            error = null
        } catch (e: Exception) {
            error = e.message
        } finally {
            isLoading = false
        }
    }
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFF0F8FF)
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 2.dp
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Practice Exercises",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            if (isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        color = Color(0xFF306998)
                    )
                }
            } else if (error != null) {
                Text(
                    text = "Unable to load exercises: $error",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Red
                )
            } else if (exercises != null) {
                Text(
                    text = exercises!!,
                    style = MaterialTheme.typography.bodyMedium
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Button(
                    onClick = { /* Open code playground */ },
                    modifier = Modifier.align(Alignment.End),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF306998)
                    )
                ) {
                    Text("Try It Now")
                }
            }
        }
    }
}

@Composable
fun PythonControlFlowContent(viewModel: ContentViewModel) {
    Column {
        ContentSection(
            title = "Control Flow in Python",
            content = "This section will teach you about if-else statements, loops, and other control structures in Python."
        )
        
        PythonCodeSnippet(
            code = """# If-Else Example
age = 18

if age >= 18:
    print("You are an adult")
else:
    print("You are a minor")
""",
            description = "Basic if-else statement"
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        DynamicCodeExamplesSection(viewModel, "Python control flow and loops")
        
        Spacer(modifier = Modifier.height(24.dp))
        
        DynamicExercisesSection(viewModel, "Python if-else statements and loops")
    }
}

@Composable
fun PythonFunctionsContent(viewModel: ContentViewModel) {
    Column {
        ContentSection(
            title = "Functions in Python",
            content = "This section will teach you how to create and use functions in Python."
        )
        
        PythonCodeSnippet(
            code = """# Function Example
def greet(name):
    return f"Hello, {name}!"

message = greet("Python Learner")
print(message)  # Output: Hello, Python Learner!
""",
            description = "Basic function definition and usage"
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        DynamicCodeExamplesSection(viewModel, "Python functions and parameters")
        
        Spacer(modifier = Modifier.height(24.dp))
        
        DynamicExercisesSection(viewModel, "Python functions")
    }
}

@Composable
fun PythonDataStructuresContent(viewModel: ContentViewModel) {
    Column {
        ContentSection(
            title = "Data Structures in Python",
            content = "This section will introduce you to lists, dictionaries, sets, and tuples in Python."
        )
        
        PythonCodeSnippet(
            code = """# List Example
fruits = ["apple", "banana", "cherry"]
print(fruits[0])  # Output: apple
fruits.append("orange")
print(fruits)  # Output: ["apple", "banana", "cherry", "orange"]
""",
            description = "Basic list operations"
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        DynamicCodeExamplesSection(viewModel, "Python lists and dictionaries")
        
        Spacer(modifier = Modifier.height(24.dp))
        
        DynamicExercisesSection(viewModel, "Python data structures")
    }
}

@Composable
fun PythonOOPContent(viewModel: ContentViewModel) {
    Column {
        ContentSection(
            title = "Object-Oriented Programming in Python",
            content = "This section will teach you about classes, objects, inheritance, and polymorphism in Python."
        )
        
        PythonCodeSnippet(
            code = """# Class Example
class Person:
    def __init__(self, name, age):
        self.name = name
        self.age = age
        
    def greet(self):
        return f"Hello, my name is {self.name}"

person = Person("Alice", 25)
print(person.greet())  # Output: Hello, my name is Alice
""",
            description = "Basic class definition and usage"
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        DynamicCodeExamplesSection(viewModel, "Python classes and objects")
        
        Spacer(modifier = Modifier.height(24.dp))
        
        DynamicExercisesSection(viewModel, "Python object-oriented programming")
    }
}

