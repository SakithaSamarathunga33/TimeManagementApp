package com.example.timemanagementapp

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.widget.Toast

class ReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        Toast.makeText(context, "Reminder Alert!", Toast.LENGTH_LONG).show()
        // Optionally, you can trigger a notification here
    }
}
