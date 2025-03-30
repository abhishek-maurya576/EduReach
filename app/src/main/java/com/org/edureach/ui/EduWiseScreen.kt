package com.org.edureach.ui

import android.content.res.Configuration
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.org.edureach.data.models.EduWiseMessage
import com.org.edureach.data.models.EduWisePersonality
import com.org.edureach.data.models.LearningStyle
import com.org.edureach.data.models.StudyRecommendation
import com.org.edureach.utils.MarkdownFormatter
import com.org.edureach.viewmodels.EduWiseViewModel
import com.org.edureach.viewmodels.ViewModelFactory
import kotlinx.coroutines.launch
import java.util.*
import java.text.SimpleDateFormat
import androidx.compose.foundation.layout.Arrangement

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EduWiseScreen(navController: NavController) {
    val context = LocalContext.current
    val viewModel: EduWiseViewModel = viewModel(factory = ViewModelFactory(context))
    val state by viewModel.state.collectAsState()
    
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()
    val chatListState = rememberLazyListState()
    val keyboardController = LocalSoftwareKeyboardController.current
    
    // Auto-create a session if none exists
    LaunchedEffect(state.sessions) {
        if (state.currentSessionId == null && !state.isLoading && state.sessions.isEmpty()) {
            // Create a default session with timestamp in title
            val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
            val sessionTitle = "EduWise Session - ${dateFormat.format(Date())}"
            viewModel.createSession(sessionTitle)
        }
    }
    
    // Handle errors
    LaunchedEffect(state.error) {
        state.error?.let {
            snackbarHostState.showSnackbar(
                message = it,
                actionLabel = "Dismiss",
                duration = SnackbarDuration.Short
            )
            viewModel.clearError()
        }
    }
    
    // Scroll to bottom on new message
    LaunchedEffect(state.messages.size) {
        if (state.messages.isNotEmpty()) {
            chatListState.animateScrollToItem(state.messages.size - 1)
        }
    }
    
    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
    
    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            EduWiseTopBar(
                viewModel = viewModel,
                navController = navController
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                // Main content area that adapts to landscape/portrait
                if (isLandscape) {
                    Row(modifier = Modifier.weight(1f)) {
                        // Chat area
                        ChatArea(
                            messages = state.messages,
                            isLoading = state.isLoading,
                            listState = chatListState,
                            modifier = Modifier
                                .weight(0.7f)
                                .fillMaxHeight()
                        )
                        
                        // Side panel
                        SidePanel(
                            viewModel = viewModel,
                            state = state,
                            modifier = Modifier
                                .weight(0.3f)
                                .fillMaxHeight()
                        )
                    }
                } else {
                    // Chat area
                    ChatArea(
                        messages = state.messages,
                        isLoading = state.isLoading,
                        listState = chatListState,
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                    )
                }
                
                // Input area
                MessageInputArea(
                    onSendMessage = { message ->
                        viewModel.sendMessage(message)
                        keyboardController?.hide()
                    },
                    isLoading = state.isLoading
                )
            }
            
            // Session list overlay
            AnimatedVisibility(
                visible = state.showSessionList,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                SessionListOverlay(
                    sessions = state.sessions,
                    onSessionSelected = { viewModel.selectSession(it) },
                    onCreateSession = { title -> viewModel.createSession(title) },
                    onDismiss = { viewModel.toggleSessionList() }
                )
            }
            
            // Learning style assessment overlay
            AnimatedVisibility(
                visible = state.showStyleAssessment,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                LearningStyleAssessment(
                    selectedStyle = state.learningStyle,
                    onStyleSelected = { viewModel.updateLearningStyle(it) },
                    onDismiss = { viewModel.toggleStyleAssessment() }
                )
            }
            
            // Recommendations overlay
            AnimatedVisibility(
                visible = state.showRecommendations,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                RecommendationsOverlay(
                    recommendations = state.recommendations,
                    onRecommendationCompleted = { viewModel.completeRecommendation(it) },
                    onGenerateNew = { viewModel.generateRecommendations() },
                    onDismiss = { viewModel.toggleRecommendations() },
                    formatDate = { viewModel.formatDate(it) }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EduWiseTopBar(
    viewModel: EduWiseViewModel,
    navController: NavController
) {
    val state by viewModel.state.collectAsState()
    val context = LocalContext.current
    var showMenu by remember { mutableStateOf(false) }
    
    TopAppBar(
        title = { 
            Text(
                "EduWise",
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.Bold
                )
            )
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.primary,
            titleContentColor = Color.White,
            actionIconContentColor = Color.White,
            navigationIconContentColor = Color.White
        ),
        navigationIcon = {
            IconButton(onClick = { navController.popBackStack() }) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back"
                )
            }
        },
        actions = {
            // New chat button
            IconButton(onClick = { 
                val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
                val sessionTitle = "EduWise Session - ${dateFormat.format(Date())}"
                viewModel.createSession(sessionTitle)
            }) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "New Chat"
                )
            }
            
            // Share button (if messages exist)
            if (state.messages.isNotEmpty()) {
                IconButton(onClick = { viewModel.shareSession(context) }) {
                    Icon(
                        imageVector = Icons.Default.Share,
                        contentDescription = "Share"
                    )
                }
            }
            
            // Overflow menu (3-dot menu)
            Box {
                IconButton(onClick = { showMenu = true }) {
                    Icon(
                        imageVector = Icons.Default.MoreVert,
                        contentDescription = "More options"
                    )
                }
                
                DropdownMenu(
                    expanded = showMenu,
                    onDismissRequest = { showMenu = false },
                    modifier = Modifier.background(MaterialTheme.colorScheme.surface)
                ) {
                    // History option
                    DropdownMenuItem(
                        text = { Text("Session History") },
                        onClick = { 
                            viewModel.toggleSessionList()
                            showMenu = false
                        },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.History,
                                contentDescription = null
                            )
                        }
                    )
                    
                    // Recommendations option
                    DropdownMenuItem(
                        text = { Text("Study Recommendations") },
                        onClick = { 
                            viewModel.toggleRecommendations()
                            showMenu = false
                        },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Lightbulb,
                                contentDescription = null
                            )
                        }
                    )
                    
                    // Learning style option
                    DropdownMenuItem(
                        text = { Text("Learning Style") },
                        onClick = { 
                            viewModel.toggleStyleAssessment()
                            showMenu = false
                        },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Psychology,
                                contentDescription = null
                            )
                        }
                    )
                }
            }
        }
    )
}

@Composable
private fun ChatArea(
    messages: List<EduWiseMessage>,
    isLoading: Boolean,
    listState: androidx.compose.foundation.lazy.LazyListState,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .background(MaterialTheme.colorScheme.background)
    ) {
        if (messages.isEmpty() && !isLoading) {
            // Empty state
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Psychology,
                    contentDescription = null,
                    modifier = Modifier.size(80.dp),
                    tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Welcome to EduWise",
                    style = MaterialTheme.typography.headlineSmall,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Your AI learning companion that adapts to your learning style and helps you study more effectively.",
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
                )
                Spacer(modifier = Modifier.height(24.dp))
                Text(
                    text = "Ask me anything about study techniques, learning strategies, or specific subjects.",
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f)
                )
            }
        } else {
            // Message list
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                state = listState,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(messages) { message ->
                    ChatMessage(message = message)
                }
                
                if (isLoading) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ChatMessage(message: EduWiseMessage) {
    val isUserMessage = message.isUserMessage
    val backgroundColor = if (isUserMessage) 
        MaterialTheme.colorScheme.primaryContainer
    else 
        MaterialTheme.colorScheme.secondaryContainer
    
    val textColor = if (isUserMessage)
        MaterialTheme.colorScheme.onPrimaryContainer
    else
        MaterialTheme.colorScheme.onSecondaryContainer
    
    val alignment = if (isUserMessage) Alignment.TopEnd else Alignment.TopStart
    
    Box(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = alignment
    ) {
        Column(
            modifier = Modifier
                .widthIn(max = 280.dp)
                .clip(
                    RoundedCornerShape(
                        topStart = if (isUserMessage) 12.dp else 0.dp,
                        topEnd = if (isUserMessage) 0.dp else 12.dp,
                        bottomStart = 12.dp,
                        bottomEnd = 12.dp
                    )
                )
                .background(backgroundColor)
                .padding(12.dp)
        ) {
            if (isUserMessage) {
                Text(
                    text = message.content,
                    color = textColor
                )
            } else {
                MarkdownFormatter.MarkdownText(
                    markdown = message.content,
                    modifier = Modifier.fillMaxWidth(),
                    color = textColor
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MessageInputArea(
    onSendMessage: (String) -> Unit,
    isLoading: Boolean
) {
    var messageText by remember { mutableStateOf("") }
    
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 8.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = messageText,
                onValueChange = { messageText = it },
                modifier = Modifier
                    .weight(1f)
                    .padding(end = 8.dp),
                placeholder = { Text("Ask EduWise...") },
                maxLines = 3,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                keyboardActions = KeyboardActions(
                    onSend = {
                        if (messageText.isNotBlank() && !isLoading) {
                            onSendMessage(messageText)
                            messageText = ""
                        }
                    }
                ),
                colors = TextFieldDefaults.outlinedTextFieldColors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                )
            )
            
            FloatingActionButton(
                onClick = {
                    if (messageText.isNotBlank() && !isLoading) {
                        onSendMessage(messageText)
                        messageText = ""
                    }
                },
                modifier = Modifier.size(48.dp),
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = Color.White
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = Color.White,
                        strokeWidth = 2.dp
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.Send,
                        contentDescription = "Send"
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SidePanel(
    viewModel: EduWiseViewModel,
    state: com.org.edureach.viewmodels.EduWiseState,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .background(Color(0xFFF5F5F5))
            .padding(16.dp)
    ) {
        // Personality Selector
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            elevation = CardDefaults.elevatedCardElevation(defaultElevation = 4.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text(
                    text = "EduWise Personality",
                    style = MaterialTheme.typography.titleMedium
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                PersonalitySelector(
                    selectedPersonality = state.personality,
                    onPersonalitySelected = { viewModel.updatePersonality(it) }
                )
            }
        }
        
        // Active Recommendations Preview
        if (state.recommendations.isNotEmpty()) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                elevation = CardDefaults.elevatedCardElevation(defaultElevation = 4.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Study Recommendations",
                            style = MaterialTheme.typography.titleMedium
                        )
                        
                        TextButton(
                            onClick = { viewModel.toggleRecommendations() }
                        ) {
                            Text("View All")
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    state.recommendations.take(2).forEach { recommendation ->
                        RecommendationItem(
                            recommendation = recommendation,
                            onComplete = { viewModel.completeRecommendation(recommendation.id) },
                            formatDate = { viewModel.formatDate(it) }
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }
            }
        }
        
        // Learning Style Info
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            elevation = CardDefaults.elevatedCardElevation(defaultElevation = 4.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text(
                    text = "Your Learning Style",
                    style = MaterialTheme.typography.titleMedium
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                val style = state.learningStyle ?: LearningStyle.UNKNOWN
                val styleText = when (style) {
                    LearningStyle.VISUAL -> "Visual learner (learns through seeing)"
                    LearningStyle.AUDITORY -> "Auditory learner (learns through hearing)"
                    LearningStyle.KINESTHETIC -> "Kinesthetic learner (learns through doing)"
                    LearningStyle.READING_WRITING -> "Reading/Writing learner (learns through text)"
                    LearningStyle.SIMPLE -> "Simple learner (learns through straightforward explanations)"
                    LearningStyle.UNKNOWN -> "Not yet determined"
                }
                
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.School, 
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = styleText,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                if (style == LearningStyle.UNKNOWN) {
                    Button(
                        onClick = { viewModel.toggleStyleAssessment() },
                        modifier = Modifier.align(Alignment.End)
                    ) {
                        Text("Discover Your Style")
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PersonalitySelector(
    selectedPersonality: EduWisePersonality,
    onPersonalitySelected: (EduWisePersonality) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp)
                .clip(RoundedCornerShape(8.dp))
                .clickable { onPersonalitySelected(EduWisePersonality.SUPPORTIVE) }
                .background(
                    if (selectedPersonality == EduWisePersonality.SUPPORTIVE)
                        MaterialTheme.colorScheme.primaryContainer
                    else
                        Color.Transparent
                )
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            RadioButton(
                selected = selectedPersonality == EduWisePersonality.SUPPORTIVE,
                onClick = { onPersonalitySelected(EduWisePersonality.SUPPORTIVE) }
            )
            Spacer(modifier = Modifier.width(8.dp))
            Column {
                Text(
                    text = "Supportive",
                    style = MaterialTheme.typography.bodyLarge
                )
                Text(
                    text = "Encouraging and helpful",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
                )
            }
        }
        
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp)
                .clip(RoundedCornerShape(8.dp))
                .clickable { onPersonalitySelected(EduWisePersonality.CHALLENGING) }
                .background(
                    if (selectedPersonality == EduWisePersonality.CHALLENGING)
                        MaterialTheme.colorScheme.primaryContainer
                    else
                        Color.Transparent
                )
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            RadioButton(
                selected = selectedPersonality == EduWisePersonality.CHALLENGING,
                onClick = { onPersonalitySelected(EduWisePersonality.CHALLENGING) }
            )
            Spacer(modifier = Modifier.width(8.dp))
            Column {
                Text(
                    text = "Challenging",
                    style = MaterialTheme.typography.bodyLarge
                )
                Text(
                    text = "Pushes you to excel",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
                )
            }
        }
        
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp)
                .clip(RoundedCornerShape(8.dp))
                .clickable { onPersonalitySelected(EduWisePersonality.SOCRATIC) }
                .background(
                    if (selectedPersonality == EduWisePersonality.SOCRATIC)
                        MaterialTheme.colorScheme.primaryContainer
                    else
                        Color.Transparent
                )
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            RadioButton(
                selected = selectedPersonality == EduWisePersonality.SOCRATIC,
                onClick = { onPersonalitySelected(EduWisePersonality.SOCRATIC) }
            )
            Spacer(modifier = Modifier.width(8.dp))
            Column {
                Text(
                    text = "Socratic",
                    style = MaterialTheme.typography.bodyLarge
                )
                Text(
                    text = "Guides through questions",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun RecommendationItem(
    recommendation: StudyRecommendation,
    onComplete: () -> Unit,
    formatDate: (Date) -> String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(MaterialTheme.colorScheme.surface)
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Checkbox(
            checked = recommendation.isCompleted,
            onCheckedChange = { if (!recommendation.isCompleted) onComplete() },
            colors = CheckboxDefaults.colors(
                checkedColor = MaterialTheme.colorScheme.primary
            )
        )
        
        Spacer(modifier = Modifier.width(8.dp))
        
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = recommendation.description,
                style = MaterialTheme.typography.bodyMedium
            )
            
            if (recommendation.dueDate != null) {
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.DateRange,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = formatDate(recommendation.dueDate),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
        
        val priorityColor = when (recommendation.priority) {
            1 -> Color(0xFF2E7D32) // Low - Green
            2 -> Color(0xFFF57F17) // Medium - Amber
            3 -> Color(0xFFD32F2F) // High - Red
            else -> Color.Gray
        }
        
        Box(
            modifier = Modifier
                .size(12.dp)
                .clip(CircleShape)
                .background(priorityColor)
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SessionListOverlay(
    sessions: List<com.org.edureach.data.models.EduWiseSession>,
    onSessionSelected: (String) -> Unit,
    onCreateSession: (String) -> Unit,
    onDismiss: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.5f))
            .clickable(onClick = onDismiss)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.8f)
                .fillMaxHeight(0.7f)
                .align(Alignment.Center)
                .clickable(onClick = {}), // Prevent click propagation
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Session History",
                        style = MaterialTheme.typography.titleLarge
                    )
                    
                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close"
                        )
                    }
                }
                
                Divider(modifier = Modifier.padding(vertical = 8.dp))
                
                // New session button
                var showNewSessionDialog by remember { mutableStateOf(false) }
                
                Button(
                    onClick = { showNewSessionDialog = true },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = null
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("New Session")
                }
                
                // Session list
                if (sessions.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No sessions yet. Start a new conversation!",
                            style = MaterialTheme.typography.bodyLarge,
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(sessions) { session ->
                            SessionItem(
                                session = session,
                                onSessionSelected = {
                                    onSessionSelected(session.id)
                                    onDismiss()
                                }
                            )
                        }
                    }
                }
                
                if (showNewSessionDialog) {
                    NewSessionDialog(
                        onCreateSession = { title ->
                            onCreateSession(title)
                            onDismiss()
                        },
                        onDismiss = { showNewSessionDialog = false }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SessionItem(
    session: com.org.edureach.data.models.EduWiseSession,
    onSessionSelected: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onSessionSelected),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = session.title,
                style = MaterialTheme.typography.titleMedium
            )
            
            Spacer(modifier = Modifier.height(4.dp))
            
            val dateFormat = remember { SimpleDateFormat("MMM d, yyyy 'at' h:mm a", Locale.getDefault()) }
            Text(
                text = dateFormat.format(session.timestamp),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
            )
            
            if (session.lastQuery != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "\"${session.lastQuery}\"",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.8f),
                    maxLines = 1
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun NewSessionDialog(
    onCreateSession: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var sessionTitle by remember { mutableStateOf("") }
    
    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.background
            )
        ) {
            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth()
            ) {
                Text(
                    text = "New Learning Session",
                    style = MaterialTheme.typography.titleLarge
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                OutlinedTextField(
                    value = sessionTitle,
                    onValueChange = { sessionTitle = it },
                    label = { Text("Session Title") },
                    placeholder = { Text("e.g., Study Techniques, Python Basics") },
                    modifier = Modifier.fillMaxWidth()
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(
                        onClick = onDismiss
                    ) {
                        Text("Cancel")
                    }
                    
                    Spacer(modifier = Modifier.width(8.dp))
                    
                    Button(
                        onClick = {
                            if (sessionTitle.isNotBlank()) {
                                onCreateSession(sessionTitle)
                            }
                        },
                        enabled = sessionTitle.isNotBlank()
                    ) {
                        Text("Create")
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun LearningStyleAssessment(
    selectedStyle: LearningStyle?,
    onStyleSelected: (LearningStyle) -> Unit,
    onDismiss: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.5f))
            .clickable(onClick = onDismiss)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.8f)
                .fillMaxHeight(0.8f)
                .align(Alignment.Center)
                .clickable(onClick = {}), // Prevent click propagation
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Your Learning Style",
                        style = MaterialTheme.typography.titleLarge
                    )
                    
                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close"
                        )
                    }
                }
                
                Divider(modifier = Modifier.padding(vertical = 8.dp))
                
                // Intro text
                Text(
                    text = "Understanding how you learn best helps EduWise personalize its responses to match your learning style.",
                    style = MaterialTheme.typography.bodyLarge
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Learning styles
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    item {
                        LearningStyleOption(
                            style = LearningStyle.SIMPLE,
                            title = "Simple Learner",
                            description = "You learn best through straightforward, clear explanations - simple language and direct instructions help you understand concepts quickly.",
                            icon = Icons.Default.LightMode,
                            isSelected = selectedStyle == LearningStyle.SIMPLE || selectedStyle == null,
                            onSelect = { onStyleSelected(LearningStyle.SIMPLE) }
                        )
                    }
                    
                    item {
                        LearningStyleOption(
                            style = LearningStyle.VISUAL,
                            title = "Visual Learner",
                            description = "You learn best through seeing information presented visually - images, diagrams, charts, and demonstrations help you understand concepts.",
                            icon = Icons.Default.Image,
                            isSelected = selectedStyle == LearningStyle.VISUAL,
                            onSelect = { onStyleSelected(LearningStyle.VISUAL) }
                        )
                    }
                    
                    item {
                        LearningStyleOption(
                            style = LearningStyle.AUDITORY,
                            title = "Auditory Learner",
                            description = "You learn best through hearing information - discussions, lectures, and verbal explanations help you process and remember concepts.",
                            icon = Icons.Default.Hearing,
                            isSelected = selectedStyle == LearningStyle.AUDITORY,
                            onSelect = { onStyleSelected(LearningStyle.AUDITORY) }
                        )
                    }
                    
                    item {
                        LearningStyleOption(
                            style = LearningStyle.KINESTHETIC,
                            title = "Kinesthetic Learner",
                            description = "You learn best through doing and experiencing - hands-on activities, practical applications, and physical movement help you grasp concepts.",
                            icon = Icons.Default.TouchApp,
                            isSelected = selectedStyle == LearningStyle.KINESTHETIC,
                            onSelect = { onStyleSelected(LearningStyle.KINESTHETIC) }
                        )
                    }
                    
                    item {
                        LearningStyleOption(
                            style = LearningStyle.READING_WRITING,
                            title = "Reading/Writing Learner",
                            description = "You learn best through text-based information - reading articles, writing notes, and processing written information helps you understand concepts.",
                            icon = Icons.Default.MenuBook,
                            isSelected = selectedStyle == LearningStyle.READING_WRITING,
                            onSelect = { onStyleSelected(LearningStyle.READING_WRITING) }
                        )
                    }
                }
                
                // Button
                Button(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Save and Continue")
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun LearningStyleOption(
    style: LearningStyle,
    title: String,
    description: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    isSelected: Boolean,
    onSelect: () -> Unit
) {
    val borderColor = if (isSelected) MaterialTheme.colorScheme.primary else Color.LightGray
    val backgroundColor = if (isSelected) 
        MaterialTheme.colorScheme.primary.copy(alpha = 0.1f) 
    else 
        Color.Transparent
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onSelect)
            .border(
                width = 2.dp,
                color = borderColor,
                shape = RoundedCornerShape(12.dp)
            ),
        colors = CardDefaults.cardColors(
            containerColor = backgroundColor
        ),
        elevation = CardDefaults.elevatedCardElevation(
            defaultElevation = if (isSelected) 4.dp else 0.dp
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icon
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(
                        if (isSelected) 
                            MaterialTheme.colorScheme.primary 
                        else 
                            Color.LightGray
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium
                )
                
                Spacer(modifier = Modifier.height(4.dp))
                
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
                )
            }
            
            Spacer(modifier = Modifier.width(8.dp))
            
            RadioButton(
                selected = isSelected,
                onClick = onSelect
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun RecommendationsOverlay(
    recommendations: List<StudyRecommendation>,
    onRecommendationCompleted: (String) -> Unit,
    onGenerateNew: () -> Unit,
    onDismiss: () -> Unit,
    formatDate: (Date) -> String
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.5f))
            .clickable(onClick = onDismiss)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.8f)
                .fillMaxHeight(0.8f)
                .align(Alignment.Center)
                .clickable(onClick = {}), // Prevent click propagation
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Study Recommendations",
                        style = MaterialTheme.typography.titleLarge
                    )
                    
                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close"
                        )
                    }
                }
                
                Divider(modifier = Modifier.padding(vertical = 8.dp))
                
                // Generate new recommendations button
                Button(
                    onClick = onGenerateNew,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = null
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Generate New Recommendations")
                }
                
                // Recommendations list
                if (recommendations.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No active recommendations. Generate some new ones!",
                            style = MaterialTheme.typography.bodyLarge,
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(recommendations) { recommendation ->
                            DetailedRecommendationItem(
                                recommendation = recommendation,
                                onComplete = { onRecommendationCompleted(recommendation.id) },
                                formatDate = formatDate
                            )
                        }
                    }
                }
                
                // Close button
                TextButton(
                    onClick = onDismiss,
                    modifier = Modifier
                        .align(Alignment.End)
                        .padding(top = 8.dp)
                ) {
                    Text("Close")
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DetailedRecommendationItem(
    recommendation: StudyRecommendation,
    onComplete: () -> Unit,
    formatDate: (Date) -> String
) {
    val priorityColor = when (recommendation.priority) {
        1 -> Color(0xFF2E7D32) // Low - Green
        2 -> Color(0xFFF57F17) // Medium - Amber
        3 -> Color(0xFFD32F2F) // High - Red
        else -> Color.Gray
    }
    
    val priorityText = when (recommendation.priority) {
        1 -> "Low Priority"
        2 -> "Medium Priority"
        3 -> "High Priority"
        else -> "Priority"
    }
    
    Card(
        modifier = Modifier
            .fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (recommendation.isCompleted)
                MaterialTheme.colorScheme.surface.copy(alpha = 0.5f)
            else
                MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Checkbox(
                    checked = recommendation.isCompleted,
                    onCheckedChange = { if (!recommendation.isCompleted) onComplete() },
                    colors = CheckboxDefaults.colors(
                        checkedColor = MaterialTheme.colorScheme.primary
                    )
                )
                
                Spacer(modifier = Modifier.width(8.dp))
                
                Text(
                    text = recommendation.description,
                    style = MaterialTheme.typography.bodyLarge,
                    color = if (recommendation.isCompleted)
                        MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f)
                    else
                        MaterialTheme.colorScheme.onBackground
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                recommendation.dueDate?.let { date ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.DateRange,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Due: ${formatDate(date)}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
                
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(priorityColor)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = priorityText,
                        style = MaterialTheme.typography.bodySmall,
                        color = priorityColor
                    )
                }
            }
        }
    }
} 