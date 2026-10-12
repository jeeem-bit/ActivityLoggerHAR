package com.example.activityloggerhar

import androidx.compose.ui.platform.LocalContext
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.activityloggerhar.ui.theme.ActivityLoggerHARTheme
import android.Manifest
import android.content.Intent
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat

@Composable
fun LoggerScreen() {

    val context = LocalContext.current

    // to remember the selected activity
    var currentLabel by remember {
        mutableStateOf("None")
    }

    // to remember if the recording is active
    var isRecording by remember {
        mutableStateOf(false)
    }

    fun startRecording() {
        ContextCompat.startForegroundService(
            context,
            Intent(context, RecordingService::class.java)
                .setAction(RecordingService.ACTION_START)
        )
        isRecording = true
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { results ->
        // this is required for health activity
        if (results[Manifest.permission.ACTIVITY_RECOGNITION] == true) startRecording()
    }

    // list of preselected activities
    val activities = listOf(
        "Walking",
        "Running",
        "Stationary",
        "Commuting"
    )

    Column(
        modifier = Modifier.padding(
            start = 16.dp,
            top = 50.dp,
            end = 16.dp,
            bottom = 16.dp
        )
    ) {

        Text("Current Activity: $currentLabel")

        Spacer(modifier = Modifier.height(16.dp))

        activities.forEach { activity ->

            Button(
                modifier = Modifier.padding(vertical = 4.dp),
                onClick = {
                    // set current activity, update recording service currentLabel
                    currentLabel = activity
                    RecordingService.currentLabel = activity
                }
            ) {
                Text(activity)
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            // start recording button
            Button(
                onClick = {
                    RecordingService.currentLabel = currentLabel
                    val perms = buildList {
                        add(Manifest.permission.ACTIVITY_RECOGNITION)
                        if (Build.VERSION.SDK_INT >= 33) add(Manifest.permission.POST_NOTIFICATIONS)
                    }.toTypedArray()
                    permissionLauncher.launch(perms)
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF4CAF50)
                )
            ) {
                Text("Start Recording")
            }

            // stop recording button
            Button(
                onClick = {
                    isRecording = false
                    context.startService(
                        Intent(context, RecordingService::class.java)
                            .setAction(RecordingService.ACTION_STOP)
                    )
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFF44336)
                )
            ) {
                Text("Stop Recording")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            if (isRecording)
                "Recording..."
            else
                "Not Recording"
        )
    }
}

@Preview(showBackground = true)
@Composable
fun LoggerScreenPreview() {
    ActivityLoggerHARTheme {
        LoggerScreen()
    }
}