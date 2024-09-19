package com.example.timemanagementapp

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import java.util.*

class ReminderActivity : AppCompatActivity() {

    private lateinit var reminderTimeInput: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_reminder)

        reminderTimeInput = findViewById(R.id.reminder_time_input)

        findViewById<Button>(R.id.save_reminder_button).setOnClickListener {
            val time = reminderTimeInput.text.toString()
            setReminder(time)
        }
    }

    private fun setReminder(time: String) {
        val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(this, ReminderReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(this, 0, intent, 0)

        val calendar = Calendar.getInstance()
        // Parse time and set calendar time here (example assumes 24-hour format)
        val (hour, minute) = time.split(":").map { it.toInt() }
        calendar.set(Calendar.HOUR_OF_DAY, hour)
        calendar.set(Calendar.MINUTE, minute)
        calendar.set(Calendar.SECOND, 0)

        alarmManager.set(AlarmManager.RTC_WAKEUP, calendar.timeInMillis, pendingIntent)
    }
}
