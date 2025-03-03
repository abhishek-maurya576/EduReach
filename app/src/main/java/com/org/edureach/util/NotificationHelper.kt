package com.org.edureach.util

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.work.*
import com.org.edureach.MainActivity
import com.org.edureach.R
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.concurrent.TimeUnit

class NotificationHelper(private val context: Context) {
    companion object {
        const val CHANNEL_ID = "task_notifications"
        const val CHANNEL_NAME = "Task Notifications"
        const val CHANNEL_DESCRIPTION = "Notifications for task due dates"
    }

    init {
        createNotificationChannel()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = CHANNEL_DESCRIPTION
            }
            
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    fun scheduleTaskNotification(taskId: String, taskTitle: String, dueDate: String) {
        val workManager = WorkManager.getInstance(context)
        
        // Parse the due date
        val dueDateLocal = LocalDate.parse(dueDate, DateTimeFormatter.ISO_LOCAL_DATE)
        // Set notification time to 9:00 AM on the due date
        val notificationTime = LocalDateTime.of(dueDateLocal, LocalTime.of(9, 0))
        
        // Calculate delay until notification time
        val now = LocalDateTime.now()
        val delay = java.time.Duration.between(now, notificationTime)
        
        // Only schedule if the due date is in the future
        if (delay.isNegative) return
        
        val notificationData = Data.Builder()
            .putString("taskId", taskId)
            .putString("taskTitle", taskTitle)
            .build()
            
        val notificationWork = OneTimeWorkRequestBuilder<TaskNotificationWorker>()
            .setInputData(notificationData)
            .setInitialDelay(delay.toMinutes(), TimeUnit.MINUTES)
            .addTag(taskId) // Use taskId as tag to be able to cancel it later
            .build()
            
        workManager.enqueueUniqueWork(
            taskId,
            ExistingWorkPolicy.REPLACE,
            notificationWork
        )
    }

    fun cancelTaskNotification(taskId: String) {
        val workManager = WorkManager.getInstance(context)
        workManager.cancelUniqueWork(taskId)
    }
}

class TaskNotificationWorker(
    context: Context,
    params: WorkerParameters
) : Worker(context, params) {
    
    override fun doWork(): Result {
        val taskId = inputData.getString("taskId") ?: return Result.failure()
        val taskTitle = inputData.getString("taskTitle") ?: return Result.failure()
        
        showNotification(taskId, taskTitle)
        
        return Result.success()
    }
    
    private fun showNotification(taskId: String, taskTitle: String) {
        val intent = Intent(applicationContext, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("taskId", taskId)
        }
        
        val pendingIntent = PendingIntent.getActivity(
            applicationContext,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE
        )
        
        val notification = NotificationCompat.Builder(applicationContext, NotificationHelper.CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_task)
            .setContentTitle("Task Due Today")
            .setContentText("Don't forget: $taskTitle")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()
            
        NotificationManagerCompat.from(applicationContext).notify(taskId.hashCode(), notification)
    }
} 