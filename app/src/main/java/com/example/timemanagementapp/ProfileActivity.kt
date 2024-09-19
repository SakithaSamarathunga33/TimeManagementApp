package com.example.timemanagementapp

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import android.widget.Button
import android.widget.ImageButton
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class ProfileActivity : AppCompatActivity() {

    private lateinit var workTasksButton: Button
    private lateinit var personalTasksButton: Button
    private lateinit var workTasksRecyclerView: RecyclerView
    private lateinit var personalTasksRecyclerView: RecyclerView
    private lateinit var workTaskAdapter: TaskAdapter
    private lateinit var personalTaskAdapter: TaskAdapter
    private val workTasks = ArrayList<Task>()
    private val personalTasks = ArrayList<Task>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        // Set up toolbar
        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)

        // Home button functionality
        val homeButton: ImageButton = findViewById(R.id.home_button)
        homeButton.setOnClickListener {
            startActivity(Intent(this, MainActivity::class.java))
        }

        // Initialize buttons and RecyclerViews
        workTasksButton = findViewById(R.id.view_work_tasks_button)
        personalTasksButton = findViewById(R.id.view_personal_tasks_button)
        workTasksRecyclerView = findViewById(R.id.work_tasks_recycler_view)
        personalTasksRecyclerView = findViewById(R.id.personal_tasks_recycler_view)

        // Set up RecyclerViews
        workTasksRecyclerView.layoutManager = LinearLayoutManager(this)
        personalTasksRecyclerView.layoutManager = LinearLayoutManager(this)

        // Set button click listeners
        workTasksButton.setOnClickListener {
            displayWorkTasks()
        }

        personalTasksButton.setOnClickListener {
            displayPersonalTasks()
        }

        // Load tasks from storage
        loadTasks()
    }

    private fun loadTasks() {
        // Load work and personal tasks from storage
        workTasks.clear()
        personalTasks.clear()

        workTasks.addAll(TaskStorage.loadWorkTasks(this))
        personalTasks.addAll(TaskStorage.loadPersonalTasks(this))

        // Optionally show a message if no tasks are available
        if (workTasks.isEmpty()) {
            Toast.makeText(this, "No work tasks available.", Toast.LENGTH_SHORT).show()
        }
        if (personalTasks.isEmpty()) {
            Toast.makeText(this, "No personal tasks available.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun displayWorkTasks() {
        if (workTasks.isEmpty()) {
            Toast.makeText(this, "No work tasks available.", Toast.LENGTH_SHORT).show()
        } else {
            workTaskAdapter = TaskAdapter(this, workTasks)
            workTasksRecyclerView.adapter = workTaskAdapter
            workTasksRecyclerView.visibility = RecyclerView.VISIBLE
            personalTasksRecyclerView.visibility = RecyclerView.GONE
        }
    }

    private fun displayPersonalTasks() {
        if (personalTasks.isEmpty()) {
            Toast.makeText(this, "No personal tasks available.", Toast.LENGTH_SHORT).show()
        } else {
            personalTaskAdapter = TaskAdapter(this, personalTasks)
            personalTasksRecyclerView.adapter = personalTaskAdapter
            personalTasksRecyclerView.visibility = RecyclerView.VISIBLE
            workTasksRecyclerView.visibility = RecyclerView.GONE
        }
    }
}
