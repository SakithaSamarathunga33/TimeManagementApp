package com.example.timemanagementapp

import Task
import android.Manifest
import android.app.*
import android.content.*
import android.content.pm.PackageManager
import android.media.RingtoneManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.SystemClock
import android.util.Log
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import java.util.*

class TaskDetailActivity : AppCompatActivity() {

    private lateinit var chronometer: Chronometer
    private var pauseOffset: Long = 0
    private var running: Boolean = false
    private lateinit var handler: Handler
    private var completionTimeInMillis: Long = 0
    private lateinit var alarmManager: AlarmManager
    private lateinit var pendingIntent: PendingIntent
    private lateinit var progressBar: ProgressBar
    private lateinit var saveButton: Button
    private lateinit var completedCheckbox: CheckBox

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_task_detail)

        // Retrieve the task object using Parcelable
        val task = intent.getParcelableExtra<Task>("task")
        if (task == null) {
            Toast.makeText(this, "Task not found", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        // Initialize views
        val taskNameTextView = findViewById<TextView>(R.id.task_name_detail)
        val taskDescriptionTextView = findViewById<TextView>(R.id.task_description_detail)
        val taskPriorityTextView = findViewById<TextView>(R.id.task_priority_detail)
        val taskCategoryTextView = findViewById<TextView>(R.id.task_category_detail)
        chronometer = findViewById(R.id.stopwatch)
        progressBar = findViewById(R.id.progress_bar)
        saveButton = findViewById(R.id.save_button)
        completedCheckbox = findViewById(R.id.task_completed_checkbox)

        taskNameTextView.text = task.name
        taskDescriptionTextView.text = task.description
        taskPriorityTextView.text = task.priority
        taskCategoryTextView.text = task.category

        // Parse the completion time into milliseconds
        completionTimeInMillis = parseCompletionTime(task.completionTime)

        handler = Handler(Looper.getMainLooper())
        alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager

        // Start button for the stopwatch
        findViewById<Button>(R.id.start_button).setOnClickListener {
            if (!running) {
                chronometer.base = SystemClock.elapsedRealtime() - pauseOffset
                chronometer.start()
                running = true
                monitorStopwatch()
            }
        }

        // Stop button for the stopwatch
        findViewById<Button>(R.id.stop_button).setOnClickListener {
            if (running) {
                chronometer.stop()
                pauseOffset = SystemClock.elapsedRealtime() - chronometer.base
                running = false
            }
        }

        // Reset button for the stopwatch
        findViewById<Button>(R.id.reset_button).setOnClickListener {
            chronometer.base = SystemClock.elapsedRealtime()
            pauseOffset = 0
            progressBar.progress = 0  // Reset progress bar on reset
        }

        // Back button to finish the activity
        findViewById<Button>(R.id.back_button).setOnClickListener {
            finish()
        }

        // Set the max progress of the progress bar based on completion time
        progressBar.max = completionTimeInMillis.toInt()

        // Checkbox listener to show the save button
        completedCheckbox.setOnCheckedChangeListener { _, isChecked ->
            saveButton.visibility = if (isChecked) View.VISIBLE else View.GONE
        }

        // Save button listener to move the task to completed
        saveButton.setOnClickListener {
            // Logic to save the task to completed tasks
            moveToCompletedTasks(task)
            finish() // Optionally finish this activity after saving
        }
    }

    private fun moveToCompletedTasks(task: Task) {
        // Implement your logic to remove the task from the current list
        // and add it to the completed tasks list
        // Example:
        // TaskRepository.removeTask(task)
        // TaskRepository.addCompletedTask(task)
        Toast.makeText(this, "${task.name} has been marked as completed!", Toast.LENGTH_SHORT).show()
    }

    private fun parseCompletionTime(time: String): Long {
        val parts = time.split(".")
        if (parts.size == 2) {
            val minutes = parts[0].toIntOrNull() ?: 0
            val seconds = parts[1].toIntOrNull() ?: 0
            return (minutes * 60 + seconds) * 1000L
        }
        return 0L
    }

    private fun monitorStopwatch() {
        handler.postDelayed(object : Runnable {
            override fun run() {
                if (running) {
                    val elapsedMillis = SystemClock.elapsedRealtime() - chronometer.base
                    Log.d(
                        "TaskDetailActivity",
                        "Elapsed: $elapsedMillis, Completion: $completionTimeInMillis"
                    )

                    // Calculate progress percentage
                    val progress = if (completionTimeInMillis > 0) {
                        (elapsedMillis * 100 / completionTimeInMillis).toInt().coerceIn(0, 100)
                    } else {
                        0
                    }

                    progressBar.progress = progress  // Update progress bar

                    if (elapsedMillis >= completionTimeInMillis) {
                        chronometer.stop()
                        running = false
                        progressBar.progress = progressBar.max  // Set progress to max on completion
                        Toast.makeText(
                            this@TaskDetailActivity,
                            "Task time completed!",
                            Toast.LENGTH_LONG
                        ).show()
                    } else {
                        handler.postDelayed(this, 1000)
                    }
                }
            }
        }, 1000)
    }

    private fun showTimePickerDialog() {
        val calendar = Calendar.getInstance()
        val timePicker = TimePickerDialog(
            this, { _, hourOfDay, minute ->
                calendar.set(Calendar.HOUR_OF_DAY, hourOfDay)
                calendar.set(Calendar.MINUTE, minute)
                calendar.set(Calendar.SECOND, 0)
                setReminder(calendar.timeInMillis)
            }, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), true
        )
        timePicker.show()
    }

    private fun setReminder(timeInMillis: Long) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            // Check if the app can schedule exact alarms
            if (!alarmManager.canScheduleExactAlarms()) {
                Toast.makeText(
                    this,
                    "Please allow this app to schedule exact alarms in settings.",
                    Toast.LENGTH_SHORT
                ).show()
                return
            }
        }

        // Check if notification permission is granted (required for Android 13 and above)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED
            ) {
                requestPermissions(arrayOf(Manifest.permission.POST_NOTIFICATIONS), 1001)
                return
            }
        }

        val intent = Intent(this, AlarmReceiver::class.java)
        pendingIntent = PendingIntent.getBroadcast(
            this,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Schedule the alarm
        try {
            alarmManager.setExact(AlarmManager.RTC_WAKEUP, timeInMillis, pendingIntent)
            Toast.makeText(this, "Reminder set!", Toast.LENGTH_SHORT).show()
        } catch (e: SecurityException) {
            Toast.makeText(
                this,
                "Failed to set reminder. Check your permissions.",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    // Create the AlarmReceiver class to handle notifications
    class AlarmReceiver : BroadcastReceiver() {

        override fun onReceive(context: Context, intent: Intent) {
            showNotification(context)
        }

        private fun showNotification(context: Context) {
            val notificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            // Create notification channel for Android 8.0 and above
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val channel = NotificationChannel(
                    "reminder_channel",
                    "Reminder Notifications",
                    NotificationManager.IMPORTANCE_HIGH
                ).apply {
                    enableVibration(true)
                    vibrationPattern = longArrayOf(100, 200, 100, 200)
                    setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION), null)
                }
                notificationManager.createNotificationChannel(channel)
            }

            val notificationBuilder = NotificationCompat.Builder(context, "reminder_channel")
                .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
                .setContentTitle("Task Reminder")
                .setContentText("It's time to complete your task!")
                .setAutoCancel(true)
                .setVibrate(longArrayOf(100, 200, 100, 200))
                .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
                .setPriority(NotificationCompat.PRIORITY_HIGH)

            notificationManager.notify(1, notificationBuilder.build())
        }
    }
}
