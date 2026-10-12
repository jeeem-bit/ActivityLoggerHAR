package com.example.activityloggerhar

data class SensorRecord(
    val timestamp: Long,

    // accelerometer
    val ax: Float,
    val ay: Float,
    val az: Float,

    // gyroscope
    val gx: Float,
    val gy: Float,
    val gz: Float,

    // gps
    val latitude: Double,
    val longitude: Double,

    val label: String
)