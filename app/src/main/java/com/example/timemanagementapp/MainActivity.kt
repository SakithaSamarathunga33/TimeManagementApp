package com.example.timemanagementapp

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class MainActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var taskAdapter: TaskAdapter
    private val tasks = ArrayList<Task>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Initialize RecyclerView
        recyclerView = findViewById(R.id.recycler_view)
        recyclerView.layoutManager = LinearLayoutManager(this)
        taskAdapter = TaskAdapter(this, tasks)
        recyclerView.adapter = taskAdapter

        // Add Task Button
        findViewById<Button>(R.id.add_task_button).setOnClickListener {
            val intent = Intent(this, AddTaskActivity::class.java)
            startActivityForResult(intent, 1) // Request code 1 for adding new task
        }

        // Timer Button
        findViewById<Button>(R.id.timer_button).setOnClickListener {
            val intent = Intent(this, TimerActivity::class.java)
            startActivity(intent)
        }

        // Reminder Button
        findViewById<Button>(R.id.reminder_button).setOnClickListener {
            val intent = Intent(this, ReminderActivity::class.java)
            startActivity(intent)
        }

        // Profile Icon Click
        findViewById<ImageView>(R.id.profile_icon).setOnClickListener {
            val workTasks = TaskStorage.loadWorkTasks(this)
            val personalTasks = TaskStorage.loadPersonalTasks(this)

            val intent = Intent(this, ProfileActivity::class.java).apply {
                putParcelableArrayListExtra("WORK_TASKS", workTasks)
                putParcelableArrayListExtra("PERSONAL_TASKS", personalTasks)
            }
            startActivity(intent)
        }

        // Load previously saved tasks
        loadTasks()
    }

    // Handle result when returning from AddTaskActivity
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK && data != null) {
            val task = data.getParcelableExtra<Task>("task") // Use Parcelable instead of Serializable
            val isEditing = data.getBooleanExtra("isEditing", false)
            if (task != null) {
                if (isEditing) {
                    // Update existing task
                    val position = tasks.indexOfFirst { it.name == task.name }
                    if (position != -1) {
                        tasks[position] = task
                        taskAdapter.notifyItemChanged(position)
                    }
                } else {
                    // Add new task
                    tasks.add(task)
                    taskAdapter.notifyItemInserted(tasks.size - 1)
                }
                // Save tasks to storage
                TaskStorage.saveTasks(this, getWorkTasks(), getPersonalTasks())
            }
        }
    }

    // Load tasks from storage
    private fun loadTasks() {
        tasks.clear()
        tasks.addAll(TaskStorage.loadWorkTasks(this))
        tasks.addAll(TaskStorage.loadPersonalTasks(this))
        taskAdapter.notifyDataSetChanged()
    }

    // Helper methods to retrieve work and personal tasks
    private fun getWorkTasks(): ArrayList<Task> {
        return tasks.filter { it.category == "Work" } as ArrayList<Task>
    }

    private fun getPersonalTasks(): ArrayList<Task> {
        return tasks.filter { it.category == "Personal" } as ArrayList<Task>
    }
}
