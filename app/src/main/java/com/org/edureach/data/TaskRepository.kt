package com.org.edureach.data

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.org.edureach.ui.Task
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.callbackFlow
import java.util.UUID

class TaskRepository {
    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    
    private val tasksCollection = firestore.collection("tasks")
    
    fun getTasks(): Flow<List<Task>> = callbackFlow {
        val userId = auth.currentUser?.uid ?: run {
            trySend(emptyList())
            return@callbackFlow
        }
        
        val subscription = tasksCollection
            .whereEqualTo("userId", userId)
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    return@addSnapshotListener
                }
                
                val tasks = snapshot?.documents?.mapNotNull { doc ->
                    Task(
                        id = doc.id,
                        title = doc.getString("title") ?: "",
                        description = doc.getString("description") ?: "",
                        dueDate = doc.getString("dueDate") ?: "",
                        isCompleted = doc.getBoolean("isCompleted") ?: false
                    )
                } ?: emptyList()
                
                trySend(tasks)
            }
            
        awaitClose { subscription.remove() }
    }
    
    suspend fun addTask(task: Task) {
        try {
            val userId = auth.currentUser?.uid ?: return
            
            val taskData = hashMapOf(
                "userId" to userId,
                "title" to task.title,
                "description" to task.description,
                "dueDate" to task.dueDate,
                "isCompleted" to task.isCompleted
            )
            
            tasksCollection.add(taskData).await()
        } catch (e: Exception) {
            // Handle error
        }
    }
    
    suspend fun updateTask(task: Task) {
        try {
            val taskData = hashMapOf(
                "title" to task.title,
                "description" to task.description,
                "dueDate" to task.dueDate,
                "isCompleted" to task.isCompleted
            )
            
            tasksCollection.document(task.id).update(taskData.toMap()).await()
        } catch (e: Exception) {
            // Handle error
        }
    }
    
    suspend fun deleteTask(taskId: String) {
        try {
            tasksCollection.document(taskId).delete().await()
        } catch (e: Exception) {
            // Handle error
        }
    }
} 