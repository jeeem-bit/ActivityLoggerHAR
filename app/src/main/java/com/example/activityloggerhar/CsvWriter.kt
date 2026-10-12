package com.example.activityloggerhar

import android.content.Context
import android.content.ContentValues
import android.os.Environment
import android.provider.MediaStore
import java.io.File
import java.io.FileWriter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.io.OutputStream
import java.io.OutputStreamWriter
import java.io.Writer
import android.util.Log


class CsvWriter(private val context: Context) {

    private var writer: Writer? = null
    private var outputStream: OutputStream? = null

    fun startNewRecording(activity: String) {

        val timeStamp = SimpleDateFormat(
            "yyyyMMdd_HHmmss",
            Locale.getDefault()
        ).format(Date())

        val fileName = "${activity}_${timeStamp}.csv"

        val contentValues = ContentValues().apply{
            put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
            put(MediaStore.MediaColumns.MIME_TYPE, "text/csv")
            put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
        }

        val resolver = context.contentResolver
        val uri = resolver.insert(
            MediaStore.Downloads.EXTERNAL_CONTENT_URI,
            contentValues
        )

        if (uri == null){
            Log.e("CSV", "Failed to create new CSV file")
            return
        }

        try {
            outputStream = resolver.openOutputStream(uri)

            writer = OutputStreamWriter(
                outputStream!!,
                Charsets.UTF_8
            )

            writer?.append(
                "timestamp,acc_x,acc_y,acc_z,gyro_x,gyro_y,gyro_z,label\n"
            )

            writer?.flush()

            Log.d(
                "CSV",
                "CSV created successfully: $fileName"
            )
        } catch (e: Exception) {
            Log.e("CSV", "Failed to create new CSV file", e)

            writer = null
            outputStream = null
        }
    }

    fun writeData(
        timestamp: Long,
        accX: Float,
        accY: Float,
        accZ: Float,
        gyroX: Float,
        gyroY: Float,
        gyroZ: Float,
        label: String
    ) {
        writer?.append(
            "$timestamp,$accX,$accY,$accZ,$gyroX,$gyroY,$gyroZ,$label\n"
        )
    }

    fun stopRecording() {

        try{
            writer?.flush()
            writer?.close()
            outputStream?.close()
        } catch (e: Exception){
            Log.e(
                "CSV",
                "Error closing CSV",
                e
            )
        }

        writer = null
        outputStream = null
    }
}