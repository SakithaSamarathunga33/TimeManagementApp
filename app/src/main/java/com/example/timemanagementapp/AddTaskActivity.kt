package com.example.timemanagementapp

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class AddTaskActivity : AppCompatActivity() {

    private lateinit var taskNameInput: EditText
    private lateinit var taskDescriptionInput: EditText
    private lateinit var prioritySpinner: Spinner
    private lateinit var taskCompletionTimeInput: EditText
    private lateinit var categorySpinner: Spinner // Spinner for category selection
    private var isEditing: Boolean = false
    private lateinit var currentTask: Task

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_task)

        taskNameInput = findViewById(R.id.task_name_input)
        taskDescriptionInput = findViewById(R.id.task_description_input)
        prioritySpinner = findViewById(R.id.priority_spinner)
        taskCompletionTimeInput = findViewById(R.id.task_completion_time_input)
        categorySpinner = findViewById(R.id.category_spinner) // Initialize category spinner

        val saveTaskButton = findViewById<Button>(R.id.save_task_button)
        val backButton = findViewById<Button>(R.id.back_button)

        // Retrieve task from intent if editing
        intent.getParcelableExtra<Task>("task")?.let { // Use Parcelable instead of Serializable
            currentTask = it
            taskNameInput.setText(currentTask.name)
            taskDescriptionInput.setText(currentTask.description)
            val priorityArray = resources.getStringArray(R.array.priority_array)
            prioritySpinner.setSelection(priorityArray.indexOf(currentTask.priority))
            taskCompletionTimeInput.setText(currentTask.completionTime)

            // Get category index from category array
            val categoryArray = resources.getStringArray(R.array.category_array)
            categorySpinner.setSelection(categoryArray.indexOf(currentTask.category)) // Set the existing category
            isEditing = true
        }

        saveTaskButton.setOnClickListener {
            val name = taskNameInput.text.toString().trim()
            val description = taskDescriptionInput.text.toString().trim()
            val priority = prioritySpinner.selectedItem.toString()
            val completionTime = taskCompletionTimeInput.text.toString().trim()
            val category = categorySpinner.selectedItem.toString() // Get the selected category

            if (name.isEmpty() || description.isEmpty() || completionTime.isEmpty()) {
                Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (!isValidTimeFormat(completionTime)) {
                Toast.makeText(this, "Invalid time format. Use mm.ss", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val task = Task(name, description, priority, completionTime, category) // Include category
            val resultIntent = Intent().apply {
                putExtra("task", task)
                putExtra("isEditing", isEditing)
            }
            setResult(RESULT_OK, resultIntent)
            finish()
        }

        backButton.setOnClickListener {
            setResult(RESULT_CANCELED)
            finish()
        }
    }

    private fun isValidTimeFormat(time: String): Boolean {
        val regex = Regex("^\\d{2}\\.\\d{2}$") // Match mm.ss format
        return regex.matches(time)
    }
}
