package com.smu.studyapp.ui.setup.screens

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat

@Composable
fun NotificationsScreen(onNext: () -> Unit) {
    val context = LocalContext.current
    fun currentlyGranted(): Boolean =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                context, Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        } else true

    var granted by remember { mutableStateOf(currentlyGranted()) }
    var requested by remember { mutableStateOf(false) }

    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { result ->
        granted = result
        requested = true
        if (result) onNext()
    }

    // Auto-skip if already granted
    LaunchedEffect(Unit) {
        if (granted) onNext()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(28.dp)
    ) {
        Spacer(Modifier.height(24.dp))
        Box(
            modifier = Modifier
                .size(80.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primaryContainer)
                .align(Alignment.CenterHorizontally),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.Default.Notifications,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(40.dp)
            )
        }
        Spacer(Modifier.height(24.dp))
        Text(
            "Stay in the loop — enable notifications",
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(Modifier.height(16.dp))
        Text(
            "The app needs permission to send you study prompts and daily surveys. Without notifications, you may miss tasks and lose compensation.",
            fontSize = 15.sp,
            lineHeight = 22.sp
        )
        Spacer(Modifier.height(20.dp))
        Text(
            "Tap \"Allow\" on the permission prompt that appears next.",
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium
        )
        Spacer(Modifier.height(20.dp))

        if (requested && !granted) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF3E0))
            ) {
                Column(Modifier.padding(16.dp)) {
                    Text(
                        "If you accidentally tapped \"Deny,\" you can fix this anytime:",
                        fontSize = 13.sp, fontWeight = FontWeight.SemiBold,
                        color = Color(0xFFE65100)
                    )
                    Spacer(Modifier.height(6.dp))
                    Text(
                        "Settings → Apps → SMU Study App → Notifications → toggle ON",
                        fontSize = 13.sp, color = Color(0xFFE65100)
                    )
                }
            }
        }

        Spacer(Modifier.weight(1f))
        Spacer(Modifier.height(24.dp))

        if (granted) {
            Button(
                onClick = onNext,
                modifier = Modifier.fillMaxWidth().height(52.dp)
            ) {
                Text("Continue", fontSize = 16.sp)
            }
        } else {
            Button(
                onClick = {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        launcher.launch(Manifest.permission.POST_NOTIFICATIONS)
                    } else {
                        granted = true
                        onNext()
                    }
                },
                modifier = Modifier.fillMaxWidth().height(52.dp)
            ) {
                Text(
                    if (requested) "Try again" else "Enable notifications",
                    fontSize = 16.sp
                )
            }
            if (requested) {
                Spacer(Modifier.height(8.dp))
                TextButton(
                    onClick = onNext,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("I'll fix this later — continue anyway", fontSize = 13.sp)
                }
            }
        }
        Spacer(Modifier.height(8.dp))
    }
}
