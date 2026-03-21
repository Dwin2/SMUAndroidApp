package com.smu.studyapp.ui.setup.screens

import android.accessibilityservice.AccessibilityServiceInfo
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import android.view.accessibility.AccessibilityManager
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun PermissionsScreen(onNext: () -> Unit) {
    val context = LocalContext.current
    var accessibilityEnabled by remember { mutableStateOf(false) }
    var overlayEnabled by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        // Check current state
        accessibilityEnabled = isAccessibilityServiceEnabled(context)
        overlayEnabled = Settings.canDrawOverlays(context)
    }

    // Re-check on resume via a side effect
    val lifecycleOwner = androidx.compose.ui.platform.LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = androidx.lifecycle.LifecycleEventObserver { _, event ->
            if (event == androidx.lifecycle.Lifecycle.Event.ON_RESUME) {
                accessibilityEnabled = isAccessibilityServiceEnabled(context)
                overlayEnabled = Settings.canDrawOverlays(context)
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
    ) {
        Text("Required Permissions", fontSize = 24.sp, fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary)
        Text("Step 3 of 3", fontSize = 13.sp, color = MaterialTheme.colorScheme.secondary)
        Spacer(Modifier.height(8.dp))
        Text("Two permissions are needed for the study to work. No personal data or app content is collected.",
            fontSize = 14.sp)
        Spacer(Modifier.height(28.dp))

        PermissionRow(
            title = "Accessibility Service",
            description = "Detects when you open/close social media apps. No content is read.",
            granted = accessibilityEnabled,
            onGrant = {
                context.startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
            }
        )
        Spacer(Modifier.height(16.dp))

        PermissionRow(
            title = "Display Over Other Apps",
            description = "Allows the brief reflection prompt to appear over social media apps.",
            granted = overlayEnabled,
            onGrant = {
                context.startActivity(
                    Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                        Uri.parse("package:${context.packageName}"))
                )
            }
        )
        Spacer(Modifier.weight(1f))

        Button(
            onClick = onNext,
            enabled = accessibilityEnabled && overlayEnabled,
            modifier = Modifier.fillMaxWidth().height(52.dp)
        ) {
            Text("Continue to Dashboard", fontSize = 16.sp)
        }

        if (!accessibilityEnabled || !overlayEnabled) {
            Spacer(Modifier.height(8.dp))
            Text("Please grant both permissions above to continue.",
                fontSize = 13.sp, color = MaterialTheme.colorScheme.error,
                modifier = Modifier.align(Alignment.CenterHorizontally))
        }
    }
}

@Composable
private fun PermissionRow(
    title: String,
    description: String,
    granted: Boolean,
    onGrant: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = if (granted) Icons.Default.Check else Icons.Default.Close,
                contentDescription = null,
                tint = if (granted) Color(0xFF4CAF50) else MaterialTheme.colorScheme.error,
                modifier = Modifier.size(28.dp)
            )
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(title, fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
                Text(description, fontSize = 13.sp, color = MaterialTheme.colorScheme.secondary)
            }
            if (!granted) {
                Spacer(Modifier.width(8.dp))
                TextButton(onClick = onGrant) { Text("Enable") }
            }
        }
    }
}

private fun isAccessibilityServiceEnabled(context: Context): Boolean {
    val am = context.getSystemService(Context.ACCESSIBILITY_SERVICE) as AccessibilityManager
    val enabledServices = am.getEnabledAccessibilityServiceList(AccessibilityServiceInfo.FEEDBACK_ALL_MASK)
    return enabledServices.any { it.resolveInfo.serviceInfo.packageName == context.packageName }
}
