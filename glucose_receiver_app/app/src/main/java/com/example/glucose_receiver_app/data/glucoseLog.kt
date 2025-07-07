package com.example.glucose_receiver_app.data

data class GlucoseLog(
    val glucose: Int = 0,
    val timestamp: Long = System.currentTimeMillis()
)
