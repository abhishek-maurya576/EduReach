package com.org.edureach.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Pending
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.lifecycle.viewmodel.compose.viewModel
import com.org.edureach.viewmodel.TaskViewModel
import java.time.LocalDate
import java.time.format.DateTimeFormatter

data class Task(
    val id: String,
    val title: String,
    val description: String,
    val dueDate: String,
    val isCompleted: Boolean = false
)

enum class TaskFilter {
    ALL, PENDING, COMPLETED
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskScreen(
    navController: NavController,
    viewModel: TaskViewModel = viewModel()
) {
    val tasks by viewModel.tasks.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    
    var showAddTaskDialog by remember { mutableStateOf(false) }
    var selectedFilter by remember { mutableStateOf(TaskFilter.ALL) }
    var selectedTask by remember { mutableStateOf<Task?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Tasks") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.Black
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddTaskDialog = true },
                containerColor = Color(0xFFDBA84F)
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add Task",
                    tint = Color.Black
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
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                // Task filters
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    FilterChip(
                        selected = selectedFilter == TaskFilter.ALL,
                        onClick = { selectedFilter = TaskFilter.ALL },
                        label = { Text("All") }
                    )
                    FilterChip(
                        selected = selectedFilter == TaskFilter.PENDING,
                        onClick = { selectedFilter = TaskFilter.PENDING },
                        label = { Text("Pending") }
                    )
                    FilterChip(
                        selected = selectedFilter == TaskFilter.COMPLETED,
                        onClick = { selectedFilter = TaskFilter.COMPLETED },
                        label = { Text("Completed") }
                    )
                }

                // Tasks list
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(
                        tasks.filter { task ->
                            when (selectedFilter) {
                                TaskFilter.ALL -> true
                                TaskFilter.PENDING -> !task.isCompleted
                                TaskFilter.COMPLETED -> task.isCompleted
                            }
                        }
                    ) { task ->
                        TaskItem(
                            task = task,
                            onTaskClick = { selectedTask = task },
                            onTaskComplete = { viewModel.updateTaskCompletion(task) },
                            onTaskDelete = { viewModel.deleteTask(task.id) }
                        )
                    }
                }
            }
        }
    }

    // Add Task Dialog
    if (showAddTaskDialog) {
        TaskDialog(
            onDismiss = { showAddTaskDialog = false },
            onTaskAdded = { title, description, dueDate ->
                viewModel.addTask(title, description, dueDate)
                showAddTaskDialog = false
            }
        )
    }

    // Task Details Dialog
    selectedTask?.let { task ->
        TaskDetailsDialog(
            task = task,
            onDismiss = { selectedTask = null },
            onTaskComplete = {
                viewModel.updateTaskCompletion(task)
                selectedTask = null
            },
            onTaskDelete = {
                viewModel.deleteTask(task.id)
                selectedTask = null
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskDialog(
    onDismiss: () -> Unit,
    onTaskAdded: (String, String, String) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var dueDate by remember { mutableStateOf(LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE)) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add New Task") },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Title") },
                    modifier = Modifier.fillMaxWidth()
                )
                
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description") },
                    modifier = Modifier.fillMaxWidth()
                )
                
                OutlinedTextField(
                    value = dueDate,
                    onValueChange = { dueDate = it },
                    label = { Text("Due Date (YYYY-MM-DD)") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (title.isNotBlank()) {
                        onTaskAdded(title, description, dueDate)
                    }
                }
            ) {
                Text("Add")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskDetailsDialog(
    task: Task,
    onDismiss: () -> Unit,
    onTaskComplete: () -> Unit,
    onTaskDelete: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(task.title) },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = task.description,
                    fontSize = 16.sp
                )
                Text(
                    text = "Due: ${task.dueDate}",
                    fontSize = 14.sp,
                    color = Color.Gray
                )
                Text(
                    text = "Status: ${if (task.isCompleted) "Completed" else "Pending"}",
                    fontSize = 14.sp,
                    color = if (task.isCompleted) Color(0xFF4CAF50) else Color.Gray
                )
            }
        },
        confirmButton = {
            Row {
                TextButton(onClick = onTaskComplete) {
                    Text(if (task.isCompleted) "Mark as Incomplete" else "Mark as Complete")
                }
                TextButton(onClick = onTaskDelete) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete Task",
                        tint = Color.Red
                    )
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Close")
            }
        }
    )
}

@Composable
fun TaskItem(
    task: Task,
    onTaskClick: () -> Unit,
    onTaskComplete: () -> Unit,
    onTaskDelete: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onTaskClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (task.isCompleted) 
                Color(0xFFE8F5E9) 
            else 
                Color(0xFFF5F5F5)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                IconButton(
                    onClick = onTaskComplete,
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        imageVector = if (task.isCompleted) 
                            Icons.Default.CheckCircle 
                        else 
                            Icons.Default.Pending,
                        contentDescription = if (task.isCompleted) 
                            "Mark as incomplete" 
                        else 
                            "Mark as complete",
                        tint = if (task.isCompleted) 
                            Color(0xFF4CAF50) 
                        else 
                            Color.Gray
                    )
                }

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(start = 16.dp)
                ) {
                    Text(
                        text = task.title,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color.Black
                    )
                    
                    Spacer(modifier = Modifier.height(4.dp))
                    
                    Text(
                        text = task.description,
                        fontSize = 14.sp,
                        color = Color.Black
                    )
                    
                    Spacer(modifier = Modifier.height(4.dp))
                    
                    Text(
                        text = "Due: ${task.dueDate}",
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                }
            }

            IconButton(onClick = onTaskDelete) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Delete Task",
                    tint = Color.Red
                )
            }
        }
    }
} 