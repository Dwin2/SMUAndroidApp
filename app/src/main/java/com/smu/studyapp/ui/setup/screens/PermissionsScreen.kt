package com.smu.studyapp.ui.setup.screens

import android.accessibilityservice.AccessibilityServiceInfo
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import android.text.TextUtils
import android.view.accessibility.AccessibilityManager
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.smu.studyapp.service.AppMonitorService
import com.smu.studyapp.service.MonitorForegroundService
import kotlinx.coroutines.delay

private data class AccessibilityState(
    val isBound: Boolean,            // service is actually running
    val isInSecureSetting: Boolean   // system setting lists it but it isn't bound (restricted-settings trap)
)

@Composable
fun PermissionsScreen(onNext: () -> Unit) {
    val context = LocalContext.current
    var accessibility by remember { mutableStateOf(checkAccessibility(context)) }
    var overlayEnabled by remember { mutableStateOf(Settings.canDrawOverlays(context)) }
    // True once the user has tapped "Enable" on the accessibility row. We only show the
    // restricted-settings banner *after* a user attempt — otherwise we false-positive
    // every cold launch (the accessibility service often takes a moment to rebind).
    var accessibilityAttempted by rememberSaveable { mutableStateOf(false) }

    // Whenever the system setting says the service is enabled but it isn't currently
    // bound (common after process restart or memory pressure), prod the
    // MonitorForegroundService alive. That brings the process up so the OS rebinds.
    LaunchedEffect(accessibility.isInSecureSetting, accessibility.isBound) {
        if (accessibility.isInSecureSetting && !accessibility.isBound) {
            runCatching {
                ContextCompat.startForegroundService(
                    context,
                    Intent(context, MonitorForegroundService::class.java)
                )
            }
        }
    }

    // Poll continuously while this screen is visible. ON_RESUME alone is not reliable
    // on cloud emulators (Appetize) where lifecycle events may not fire when the user
    // returns from system Settings.
    LaunchedEffect(Unit) {
        while (true) {
            accessibility = checkAccessibility(context)
            overlayEnabled = Settings.canDrawOverlays(context)
            delay(800)
        }
    }

    val lifecycleOwner = androidx.compose.ui.platform.LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = androidx.lifecycle.LifecycleEventObserver { _, event ->
            if (event == androidx.lifecycle.Lifecycle.Event.ON_RESUME) {
                accessibility = checkAccessibility(context)
                overlayEnabled = Settings.canDrawOverlays(context)
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp)
    ) {
        Text(
            "System Permissions",
            fontSize = 22.sp, fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(Modifier.height(8.dp))
        Text(
            "Two system permissions are needed for the study to work. No personal data or app content is ever read or recorded.",
            fontSize = 14.sp, lineHeight = 20.sp
        )
        Spacer(Modifier.height(24.dp))

        // The system setting is the source of truth from the user's perspective: if
        // ENABLED_ACCESSIBILITY_SERVICES lists us, the user has granted the permission
        // even if the service is briefly unbound (rebind after process restart, etc.).
        val accessibilityGranted = accessibility.isInSecureSetting

        PermissionRow(
            title = "Accessibility Service",
            description = "Detects when you open or close a tracked app so we can show the prompt.",
            granted = accessibilityGranted,
            onGrant = {
                accessibilityAttempted = true
                context.startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
            }
        )

        // Only show the restricted-settings guidance if the user *tried* to enable
        // accessibility and it's still off after they came back. This is the real
        // signature of the Android 13+ sideload trap.
        if (accessibilityAttempted && !accessibilityGranted) {
            Spacer(Modifier.height(8.dp))
            RestrictedSettingsBanner(
                onOpenAppInfo = {
                    context.startActivity(
                        Intent(
                            Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                            Uri.parse("package:${context.packageName}")
                        )
                    )
                }
            )
        }

        Spacer(Modifier.height(12.dp))
        PermissionRow(
            title = "Display Over Other Apps",
            description = "Lets the brief reflection prompt appear over the social media app.",
            granted = overlayEnabled,
            onGrant = {
                context.startActivity(
                    Intent(
                        Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                        Uri.parse("package:${context.packageName}")
                    )
                )
            }
        )

        Spacer(Modifier.height(16.dp))
        OutlinedButton(
            onClick = {
                accessibility = checkAccessibility(context)
                overlayEnabled = Settings.canDrawOverlays(context)
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(6.dp))
            Text("Refresh status")
        }

        Spacer(Modifier.weight(1f))
        Spacer(Modifier.height(20.dp))

        Button(
            onClick = onNext,
            enabled = accessibilityGranted && overlayEnabled,
            modifier = Modifier.fillMaxWidth().height(52.dp)
        ) {
            Text("Continue", fontSize = 16.sp)
        }
        if (!accessibilityGranted || !overlayEnabled) {
            Spacer(Modifier.height(8.dp))
            Text(
                "Please grant both permissions above to continue.",
                fontSize = 13.sp, color = MaterialTheme.colorScheme.error,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
        }
        Spacer(Modifier.height(8.dp))
    }
}

@Composable
private fun RestrictedSettingsBanner(onOpenAppInfo: () -> Unit) {
    val context = LocalContext.current
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.errorContainer
        )
    ) {
        Column(Modifier.padding(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.Warning,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    "Toggle blocked by Android",
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onErrorContainer
                )
            }
            Spacer(Modifier.height(8.dp))
            Text(
                "Android 13+ blocks accessibility for sideloaded apps until you allow restricted settings. " +
                    "Try these in order — the first one that works:",
                fontSize = 12.sp,
                lineHeight = 17.sp,
                color = MaterialTheme.colorScheme.onErrorContainer
            )
            Spacer(Modifier.height(10.dp))

            NumberedStep(
                num = "1",
                text = "Tap \"Open App info\" below. On the page that opens, look at the very TOP-RIGHT CORNER for three small vertical dots (⋮) in the toolbar — same row as the title \"App info\". Tap it → \"Allow restricted settings\"."
            )
            Spacer(Modifier.height(6.dp))
            NumberedStep(
                num = "2",
                text = "If you don't see the dots: scroll the App info page UP. Sometimes the toolbar collapses. The icon is tiny and lives next to the title."
            )
            Spacer(Modifier.height(6.dp))
            NumberedStep(
                num = "3",
                text = "Still no dots? Tap \"Try Accessibility Settings\" below. When you toggle SMU Study App on there, Android usually pops up a dialog with a direct link to fix this."
            )
            Spacer(Modifier.height(6.dp))
            NumberedStep(
                num = "4",
                text = "Last resort: long-press \"SMU Study App\" in any app list (Settings → Apps → All apps). On some Pixel builds the menu appears as a context menu instead of the three-dots."
            )

            Spacer(Modifier.height(12.dp))
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                TextButton(onClick = onOpenAppInfo) { Text("Open App info") }
                TextButton(onClick = {
                    context.startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
                }) { Text("Try Accessibility Settings") }
            }
        }
    }
}

@Composable
private fun NumberedStep(num: String, text: String) {
    Row {
        Box(
            modifier = Modifier
                .size(18.dp)
                .background(MaterialTheme.colorScheme.error, RoundedCornerShape(50)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                num,
                color = Color.White,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold
            )
        }
        Spacer(Modifier.width(8.dp))
        Text(
            text,
            fontSize = 12.sp,
            lineHeight = 17.sp,
            color = MaterialTheme.colorScheme.onErrorContainer,
            modifier = Modifier.weight(1f)
        )
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

/**
 * Returns whether our service is actually running (isBound) AND whether it's listed in
 * the secure setting. The two diverge on Android 13+ when "restricted settings" blocks
 * a sideloaded APK — the toggle visually flips but the service is never bound.
 */
private fun checkAccessibility(context: Context): AccessibilityState {
    val expectedComponent =
        "${context.packageName}/${AppMonitorService::class.java.name}"
    val expectedShort =
        "${context.packageName}/.${AppMonitorService::class.java.simpleName}"

    val am = context.getSystemService(Context.ACCESSIBILITY_SERVICE) as AccessibilityManager
    val isBound = am.getEnabledAccessibilityServiceList(AccessibilityServiceInfo.FEEDBACK_ALL_MASK)
        .any { info ->
            info.resolveInfo.serviceInfo.packageName == context.packageName &&
                info.resolveInfo.serviceInfo.name == AppMonitorService::class.java.name
        }

    val secureValue = Settings.Secure.getString(
        context.contentResolver, Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
    ) ?: ""
    val isInSecureSetting = TextUtils.SimpleStringSplitter(':').let { splitter ->
        splitter.setString(secureValue)
        var found = false
        while (splitter.hasNext()) {
            val v = splitter.next()
            if (v.equals(expectedComponent, ignoreCase = true) ||
                v.equals(expectedShort, ignoreCase = true)
            ) {
                found = true
                break
            }
        }
        found
    }

    return AccessibilityState(isBound = isBound, isInSecureSetting = isInSecureSetting)
}
