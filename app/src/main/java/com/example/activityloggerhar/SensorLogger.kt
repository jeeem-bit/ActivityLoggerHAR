package com.example.activityloggerhar

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager


class SensorLogger(
    private val context: Context,
    private val labelProvider: () -> String
): SensorEventListener {

    private val sensorManager =
        context.getSystemService(Context.SENSOR_SERVICE) as SensorManager

    private val csvWriter =
        CsvWriter(context)

    private val accelerometer =
        sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)

    private val gyroscope =
        sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE)

    // set latest accelerometer values
    private var accX = 0f
    private var accY = 0f
    private var accZ = 0f

    // Latest gyroscope values
    private var gyroX = 0f
    private var gyroY = 0f
    private var gyroZ = 0f

    // to check if the readings were received
    private var hasAccelerometerData = false
    private var hasGyroscopeData = false


    override fun onSensorChanged(event: SensorEvent?) {

        if(event == null) return

        when(event.sensor.type){

            Sensor.TYPE_ACCELEROMETER -> {

                accX = event.values[0]
                accY = event.values[1]
                accZ = event.values[2]

                hasAccelerometerData = true

                // check if gyroscope data was received, only write if gyro received
                if (hasGyroscopeData) {

                    csvWriter.writeData(
                        timestamp = event.timestamp,
                        accX = accX,
                        accY = accY,
                        accZ = accZ,
                        gyroX = gyroX,
                        gyroY = gyroY,
                        gyroZ = gyroZ,
                        label = labelProvider()
                    )
                }
            }

            Sensor.TYPE_GYROSCOPE -> {

                gyroX = event.values[0]
                gyroY = event.values[1]
                gyroZ = event.values[2]

                hasGyroscopeData = true
            }
        }
    }

    override fun onAccuracyChanged(
        sensor: Sensor?,
        accuracy: Int
    ) {
    }
    fun start() {

        // reset sensor data
        hasAccelerometerData = false
        hasGyroscopeData = false

        accX = 0f
        accY = 0f
        accZ = 0f

        gyroX = 0f
        gyroY = 0f
        gyroZ = 0f

        csvWriter.startNewRecording(
            labelProvider()
        )

        // start accelerometer
        accelerometer?.let {
            sensorManager.registerListener(
                this,
                it,
                SensorManager.SENSOR_DELAY_GAME
            )
        }

        // start gyroscope
        gyroscope?.let {
            sensorManager.registerListener(
                this,
                it,
                SensorManager.SENSOR_DELAY_GAME
            )
        }
    }

    fun stop() {

        // stop getting sensor data
        sensorManager.unregisterListener(this)

        // close and save file
        csvWriter.stopRecording()

        hasAccelerometerData = false
        hasGyroscopeData = false
    }
}