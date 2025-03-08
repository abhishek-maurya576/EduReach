package com.org.edureach.ui

import android.content.res.Configuration
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.org.edureach.ui.components.EmptyStateView
import com.org.edureach.utils.MarkdownFormatter
import com.org.edureach.viewmodels.AITutorFormViewModel
import com.org.edureach.viewmodels.ResponseLength
import com.org.edureach.viewmodels.ViewModelFactory
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AITutorScreen(
    navController: NavController
) {
    val context = LocalContext.current
    val viewModel: AITutorFormViewModel = viewModel(
        factory = ViewModelFactory(context)
    )
    
    val state by viewModel.state.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()
    val scrollState = rememberScrollState()
    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
    
    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("AI Tutor") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFFDBA84F),
                    titleContentColor = Color.Black
                ),
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.Black
                        )
                    }
                },
                actions = {
                    // History toggle button
                    IconButton(onClick = { viewModel.toggleHistoryExpanded() }) {
                        Icon(
                            imageVector = Icons.Default.History,
                            contentDescription = "Chat History",
                            tint = Color.Black
                        )
                    }
                    
                    if (state.response.isNotBlank()) {
                        IconButton(onClick = { viewModel.shareContent(context) }) {
                            Icon(
                                imageVector = Icons.Default.Share,
                                contentDescription = "Share Content",
                                tint = Color.Black
                            )
                        }
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(Color.White)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
            ) {
                // Chat history section (expandable)
                AnimatedVisibility(
                    visible = state.historyExpanded,
                    enter = expandVertically(),
                    exit = shrinkVertically()
                ) {
                    ChatHistorySection(
                        viewModel = viewModel,
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 250.dp)
                    )
                }
                
                // Main content
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .verticalScroll(scrollState)
                        .padding(16.dp)
                ) {
                    // Form Fields
                    FormSection(viewModel, state.isLoading)
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Response Section
                    if (state.isLoading) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(color = Color(0xFFDBA84F))
                        }
                    } else if (state.response.isNotBlank()) {
                        ResponseSection(state.response)
                    }
                    
                    // Error Display
                    if (state.error != null) {
                        LaunchedEffect(state.error) {
                            coroutineScope.launch {
                                snackbarHostState.showSnackbar(
                                    message = state.error ?: "An error occurred",
                                    duration = SnackbarDuration.Long
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ChatHistorySection(
    viewModel: AITutorFormViewModel,
    modifier: Modifier = Modifier
) {
    val state by viewModel.state.collectAsState()
    var showDeleteConfirmation by remember { mutableStateOf(false) }
    
    Column(
        modifier = modifier
            .background(Color(0xFFF7F2EA))
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        // History header row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Chat History",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )
            
            TextButton(
                onClick = { showDeleteConfirmation = true },
                enabled = state.recentSessions.isNotEmpty()
            ) {
                Icon(
                    imageVector = Icons.Default.DeleteOutline,
                    contentDescription = "Clear History",
                    tint = Color(0xFFDBA84F)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "Clear All",
                    color = Color(0xFFDBA84F)
                )
            }
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        // List of sessions
        if (state.recentSessions.isEmpty()) {
            EmptyStateView(
                icon = Icons.Default.History,
                iconTint = Color(0xFFDBA84F),
                title = "No Chat History",
                message = "Your previous chats will appear here once you generate content."
            )
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxWidth()
            ) {
                items(state.recentSessions) { session ->
                    SessionItem(
                        title = session.title,
                        timestamp = viewModel.formatTimestamp(session.timestamp),
                        preview = session.lastResponse ?: "",
                        onItemClick = { viewModel.loadSession(session.id) },
                        onDeleteClick = { viewModel.deleteSession(session.id) }
                    )
                }
            }
        }
    }
    
    if (showDeleteConfirmation) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmation = false },
            title = { Text("Clear Chat History") },
            text = { Text("Are you sure you want to delete all chat history? This action cannot be undone.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.clearHistory()
                        showDeleteConfirmation = false
                    }
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirmation = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun SessionItem(
    title: String,
    timestamp: String,
    preview: String,
    onItemClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    var showDeleteConfirmation by remember { mutableStateOf(false) }
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clickable { onItemClick() },
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Left icon
            Icon(
                imageVector = Icons.Default.Chat,
                contentDescription = null,
                tint = Color(0xFFDBA84F),
                modifier = Modifier.size(24.dp)
            )
            
            Spacer(modifier = Modifier.width(12.dp))
            
            // Content
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = title,
                    fontWeight = FontWeight.Medium,
                    color = Color.Black,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                
                Spacer(modifier = Modifier.height(4.dp))
                
                Text(
                    text = timestamp,
                    color = Color.Gray,
                    fontSize = 12.sp
                )
                
                Spacer(modifier = Modifier.height(4.dp))
                
                Text(
                    text = preview,
                    color = Color.DarkGray,
                    fontSize = 12.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            
            // Actions
            Row {
                IconButton(
                    onClick = { showDeleteConfirmation = true },
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete",
                        tint = Color.Gray,
                        modifier = Modifier.size(18.dp)
                    )
                }
                
                IconButton(
                    onClick = { onItemClick() },
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                        contentDescription = "Open",
                        tint = Color(0xFFDBA84F),
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
    
    if (showDeleteConfirmation) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmation = false },
            title = { Text("Delete Chat") },
            text = { Text("Are you sure you want to delete this chat?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        onDeleteClick()
                        showDeleteConfirmation = false
                    }
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirmation = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun FormSection(viewModel: AITutorFormViewModel, isLoading: Boolean) {
    val state by viewModel.state.collectAsState()
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F5F5)),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = "Educational Content Generator",
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = Color.Black
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Subject Field (Required)
            OutlinedTextField(
                value = state.form.subject,
                onValueChange = { viewModel.updateSubject(it) },
                label = { Text("Subject*") },
                placeholder = { Text("e.g., Mathematics") },
                modifier = Modifier.fillMaxWidth(),
                enabled = !isLoading,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White,
                    focusedBorderColor = Color(0xFFDBA84F),
                    unfocusedBorderColor = Color.Gray
                )
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Topic Field (Required)
            OutlinedTextField(
                value = state.form.topic,
                onValueChange = { viewModel.updateTopic(it) },
                label = { Text("Topic*") },
                placeholder = { Text("e.g., Algebra") },
                modifier = Modifier.fillMaxWidth(),
                enabled = !isLoading,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White,
                    focusedBorderColor = Color(0xFFDBA84F),
                    unfocusedBorderColor = Color.Gray
                )
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Sub-Topic Field (Optional)
            OutlinedTextField(
                value = state.form.subTopic,
                onValueChange = { viewModel.updateSubTopic(it) },
                label = { Text("Sub-Topic (Optional)") },
                placeholder = { Text("e.g., Quadratic Equations") },
                modifier = Modifier.fillMaxWidth(),
                enabled = !isLoading,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White,
                    focusedBorderColor = Color(0xFFDBA84F),
                    unfocusedBorderColor = Color.Gray
                )
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Question Field (Optional)
            OutlinedTextField(
                value = state.form.question,
                onValueChange = { viewModel.updateQuestion(it) },
                label = { Text("Question (Optional)") },
                placeholder = { Text("e.g., How do I solve quadratic equations?") },
                modifier = Modifier.fillMaxWidth(),
                enabled = !isLoading,
                minLines = 2,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White,
                    focusedBorderColor = Color(0xFFDBA84F),
                    unfocusedBorderColor = Color.Gray
                )
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Response Length Selection
            Text(
                text = "Response Length:",
                fontWeight = FontWeight.Medium,
                fontSize = 16.sp,
                color = Color.Black
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Response Length Radio Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                val selectedLength = state.form.responseLength
                
                // Brief option
                FilterChip(
                    selected = selectedLength == ResponseLength.BRIEF,
                    onClick = { viewModel.updateResponseLength(ResponseLength.BRIEF) },
                    label = { Text("Brief") },
                    enabled = !isLoading,
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = Color(0xFFDBA84F),
                        selectedLabelColor = Color.Black
                    ),
                    modifier = Modifier.weight(1f).padding(end = 4.dp)
                )
                
                // Summary option
                FilterChip(
                    selected = selectedLength == ResponseLength.SUMMARY,
                    onClick = { viewModel.updateResponseLength(ResponseLength.SUMMARY) },
                    label = { Text("Summary") },
                    enabled = !isLoading,
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = Color(0xFFDBA84F),
                        selectedLabelColor = Color.Black
                    ),
                    modifier = Modifier.weight(1f).padding(horizontal = 4.dp)
                )
                
                // Long option
                FilterChip(
                    selected = selectedLength == ResponseLength.LONG,
                    onClick = { viewModel.updateResponseLength(ResponseLength.LONG) },
                    label = { Text("Long") },
                    enabled = !isLoading,
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = Color(0xFFDBA84F),
                        selectedLabelColor = Color.Black
                    ),
                    modifier = Modifier.weight(1f).padding(start = 4.dp)
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Required fields note
            Text(
                text = "* Required fields",
                color = Color.Gray,
                fontSize = 12.sp,
                modifier = Modifier.fillMaxWidth()
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Button Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Generate Button
                Button(
                    onClick = { viewModel.generateContent() },
                    enabled = !isLoading && state.form.subject.isNotBlank() && state.form.topic.isNotBlank(),
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFDBA84F),
                        contentColor = Color.Black
                    )
                ) {
                    Text("Generate Content")
                }
                
                Spacer(modifier = Modifier.width(8.dp))
                
                // Clear Button
                OutlinedButton(
                    onClick = { viewModel.clearForm() },
                    enabled = !isLoading,
                    modifier = Modifier.width(100.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = Color(0xFFDBA84F)
                    ),
                    border = ButtonDefaults.outlinedButtonBorder.copy(
                        brush = androidx.compose.ui.graphics.SolidColor(Color(0xFFDBA84F))
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.Clear,
                        contentDescription = "Clear",
                        tint = Color(0xFFDBA84F)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Clear")
                }
            }
        }
    }
}

@Composable
private fun ResponseSection(response: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = "Generated Content",
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = Color.Black
            )
            
            HorizontalDivider(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                color = Color(0xFFDBA84F),
                thickness = 1.dp
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Use the MarkdownFormatter instead of plain Text
            MarkdownFormatter.MarkdownText(
                markdown = response,
                color = Color.Black,
                fontSize = 16,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
} 