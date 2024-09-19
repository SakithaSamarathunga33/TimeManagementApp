package com.example.timemanagementapp

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.SystemClock
import android.widget.Button
import android.widget.Chronometer
import androidx.appcompat.app.AppCompatActivity

class TimerActivity : AppCompatActivity() {

    private lateinit var chronometer: Chronometer
    private var pauseOffset: Long = 0
    private var running: Boolean = false
    private var timerCompletionTimeInMillis: Long = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_timer)

        chronometer = findViewById(R.id.stopwatch)

        // Get the completion time from the intent
        timerCompletionTimeInMillis = intent.getLongExtra("completionTimeInMillis", 0)

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

    private fun monitorStopwatch() {
        val handler = Handler(Looper.getMainLooper())
        handler.postDelayed(object : Runnable {
            override fun run() {
                if (running) {
                    val elapsedMillis = SystemClock.elapsedRealtime() - chronometer.base

                    if (elapsedMillis >= timerCompletionTimeInMillis) {
                        chronometer.stop()
                        running = false
                        // You can show a Toast or perform any other action
                    } else {
                        handler.postDelayed(this, 1000)
                    }
                }
            }
        }, 1000)
    }
}
