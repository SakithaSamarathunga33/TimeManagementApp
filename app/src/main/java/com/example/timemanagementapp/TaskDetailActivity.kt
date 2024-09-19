package com.example.timemanagementapp

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.SystemClock
import android.util.Log
import android.widget.Button
import android.widget.Chronometer
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class TaskDetailActivity : AppCompatActivity() {

    private lateinit var chronometer: Chronometer
    private var pauseOffset: Long = 0
    private var running: Boolean = false
    private lateinit var handler: Handler
    private var completionTimeInMillis: Long = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_task_detail)

        // Retrieve the task object using Parcelable
        val task = intent.getParcelableExtra<Task>("task") // Use Parcelable
        if (task == null) {
            Toast.makeText(this, "Task not found", Toast.LENGTH_SHORT).show()
            finish() // Close the activity if the task is null
            return
        }

        val taskNameTextView = findViewById<TextView>(R.id.task_name_detail)
        val taskDescriptionTextView = findViewById<TextView>(R.id.task_description_detail)
        val taskPriorityTextView = findViewById<TextView>(R.id.task_priority_detail)
        val taskCategoryTextView = findViewById<TextView>(R.id.task_category_detail)
        chronometer = findViewById(R.id.stopwatch)

        taskNameTextView.text = task.name
        taskDescriptionTextView.text = task.description
        taskPriorityTextView.text = task.priority
        taskCategoryTextView.text = task.category // Display category

        // Parse the completion time into milliseconds
        completionTimeInMillis = parseCompletionTime(task.completionTime)

        Log.d("TaskDetailActivity", "Completion Time (ms): $completionTimeInMillis")

        handler = Handler(Looper.getMainLooper())

        findViewById<Button>(R.id.start_button).setOnClickListener {
            if (!running) {
                chronometer.base = SystemClock.elapsedRealtime() - pauseOffset
                chronometer.start()
                running = true
                monitorStopwatch()
            }
        }

        findViewById<Button>(R.id.stop_button).setOnClickListener {
            if (running) {
                chronometer.stop()
                pauseOffset = SystemClock.elapsedRealtime() - chronometer.base
                running = false
            }
        }

        findViewById<Button>(R.id.reset_button).setOnClickListener {
            chronometer.base = SystemClock.elapsedRealtime()
            pauseOffset = 0
        }

        findViewById<Button>(R.id.back_button).setOnClickListener {
            finish()
        }
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
                    Log.d("TaskDetailActivity", "Elapsed: $elapsedMillis, Completion: $completionTimeInMillis")

                    if (elapsedMillis >= completionTimeInMillis) {
                        chronometer.stop()
                        running = false
                        Toast.makeText(this@TaskDetailActivity, "Task time completed!", Toast.LENGTH_LONG).show()
                    } else {
                        handler.postDelayed(this, 1000)
                    }
                }
            }
        }, 1000)
    }
}
