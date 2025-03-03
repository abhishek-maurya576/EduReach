package com.org.edureach.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
    
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()
    
    val scrollState = rememberScrollState()
    
    LaunchedEffect(lessonId) {
        viewModel.loadLesson(lessonId)
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
            
            // Handle different states
            when {
                lessonState.isLoading -> {
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
                    }
                }
                lessonState.data != null -> {
                    val lesson = lessonState.data!!
                    
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
                                .data("https://example.com/images/lesson_${lessonId}.jpg")
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
