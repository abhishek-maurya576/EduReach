package com.org.edureach.data

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.org.edureach.ui.Task
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.callbackFlow
import java.util.UUID

/**
 * Repository for task management using Firestore
 */
class TaskRepository {
    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    
    private val tasksCollection = firestore.collection("tasks")
    
    /**
     * Get all tasks for the current user
     */
    fun getTasks(): Flow<List<Task>> = callbackFlow {
        val userId = auth.currentUser?.uid ?: run {
            Log.d(TAG, "No user logged in, returning empty task list")
            trySend(emptyList())
            return@callbackFlow
        }
        
        Log.d(TAG, "Getting tasks for user: $userId")
        
        val subscription = tasksCollection
            .whereEqualTo("userId", userId)
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    Log.e(TAG, "Error fetching tasks: ${e.message}", e)
                    return@addSnapshotListener
                }
                
                val tasks = snapshot?.documents?.mapNotNull { doc ->
                    try {
                        Task(
                            id = doc.id,
                            title = doc.getString("title") ?: "",
                            description = doc.getString("description") ?: "",
                            dueDate = doc.getString("dueDate") ?: "",
                            isCompleted = doc.getBoolean("isCompleted") ?: false
                        )
                    } catch (e: Exception) {
                        Log.e(TAG, "Error parsing task document: ${e.message}", e)
                        null
                    }
                } ?: emptyList()
                
                Log.d(TAG, "Received ${tasks.size} tasks from Firestore")
                trySend(tasks)
            }
            
        awaitClose { 
            Log.d(TAG, "Removing tasks listener")
            subscription.remove() 
        }
    }
    
    /**
     * Add a new task to Firestore
     * 
     * @return Result indicating success or failure with error message
     */
    suspend fun addTask(task: Task): Result<String> {
        return try {
            val userId = auth.currentUser?.uid ?: run {
                Log.e(TAG, "Cannot add task: No user logged in")
                return Result.failure(Exception("You must be logged in to add tasks"))
            }
            
            Log.d(TAG, "Adding task for user: $userId, title: ${task.title}")
            
            val taskData = hashMapOf(
                "userId" to userId,
                "title" to task.title,
                "description" to task.description,
                "dueDate" to task.dueDate,
                "isCompleted" to task.isCompleted,
                "createdAt" to System.currentTimeMillis()
            )
            
            val documentRef = tasksCollection.add(taskData).await()
            Log.d(TAG, "Task added successfully with ID: ${documentRef.id}")
            Result.success(documentRef.id)
        } catch (e: Exception) {
            Log.e(TAG, "Error adding task: ${e.message}", e)
            Result.failure(e)
        }
    }
    
    /**
     * Update an existing task in Firestore
     * 
     * @return Result indicating success or failure with error message
     */
    suspend fun updateTask(task: Task): Result<Unit> {
        return try {
            val userId = auth.currentUser?.uid ?: run {
                Log.e(TAG, "Cannot update task: No user logged in")
                return Result.failure(Exception("You must be logged in to update tasks"))
            }
            
            Log.d(TAG, "Updating task ID: ${task.id}, title: ${task.title}")
            
            // Add user ID to ensure we're only updating our own tasks
            val taskData = hashMapOf(
                "userId" to userId, // Keep the owner reference
                "title" to task.title,
                "description" to task.description,
                "dueDate" to task.dueDate,
                "isCompleted" to task.isCompleted,
                "updatedAt" to System.currentTimeMillis()
            )
            
            tasksCollection.document(task.id).update(taskData.toMap()).await()
            Log.d(TAG, "Task updated successfully")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Error updating task: ${e.message}", e)
            Result.failure(e)
        }
    }
    
    /**
     * Delete a task from Firestore
     * 
     * @return Result indicating success or failure with error message
     */
    suspend fun deleteTask(taskId: String): Result<Unit> {
        return try {
            Log.d(TAG, "Deleting task ID: $taskId")
            tasksCollection.document(taskId).delete().await()
            Log.d(TAG, "Task deleted successfully")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Error deleting task: ${e.message}", e)
            Result.failure(e)
        }
    }
    
    companion object {
        private const val TAG = "TaskRepository"
    }
} 