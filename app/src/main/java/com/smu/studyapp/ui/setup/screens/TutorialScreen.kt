package com.smu.studyapp.ui.setup.screens

import android.accessibilityservice.AccessibilityServiceInfo
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import android.view.accessibility.AccessibilityManager
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// Pages: 0=HowItWorks, 1=AppSelection, 2=DemoMRP, 3=DemoSatisfaction, 4=AccessibilitySetup, 5=OverlaySetup, 6=AllSet
private const val TOTAL_PAGES = 7

@Composable
fun TutorialScreen(
    onComplete: () -> Unit,
    onSaveSelectedApps: (List<String>) -> Unit = {}
) {
    var page by remember { mutableIntStateOf(0) }
    var demoMotivationText by remember { mutableStateOf("") }

    Column(Modifier.fillMaxSize()) {
        ProgressDots(current = page, total = TOTAL_PAGES)

        Box(Modifier.weight(1f)) {
            when (page) {
                0 -> HowItWorksPage(onNext = { page++ })
                1 -> AppSelectionPage(onNext = { selected ->
                    onSaveSelectedApps(selected)
                    page++
                })
                2 -> DemoMRPPage(
                    text = demoMotivationText,
                    onTextChange = { demoMotivationText = it },
                    onNext = { page++ },
                    onSkip = { page++ }
                )
                3 -> DemoSatisfactionPage(onNext = { page++ })
                4 -> AccessibilitySetupPage(onNext = { page++ })
                5 -> OverlaySetupPage(onNext = { page++ })
                6 -> AllSetPage(onGo = onComplete)
            }
        }
    }
}

@Composable
private fun ProgressDots(current: Int, total: Int) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 16.dp, bottom = 8.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        repeat(total) { i ->
            Box(
                modifier = Modifier
                    .padding(horizontal = 4.dp)
                    .size(if (i == current) 10.dp else 7.dp)
                    .clip(CircleShape)
                    .background(
                        if (i <= current) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.outlineVariant
                    )
            )
        }
    }
}

// ── App data ──────────────────────────────────────────────────────────────────

val ALL_APPS = listOf(
    "com.instagram.android" to "Instagram",
    "com.zhiliaoapp.musically" to "TikTok",
    "com.google.android.youtube" to "YouTube",
    "com.facebook.katana" to "Facebook",
    "com.snapchat.android" to "Snapchat",
    "com.twitter.android" to "X / Twitter",
    "com.whatsapp" to "WhatsApp",
    "com.discord" to "Discord"
)

// ── Page 0: How It Works ──────────────────────────────────────────────────────

@Composable
private fun HowItWorksPage(onNext: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(28.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Here's how the study works", fontSize = 22.sp, fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary, textAlign = TextAlign.Center)
        Spacer(Modifier.height(24.dp))

        StepCard(
            number = "1",
            title = "Open a social media app",
            body = "When you open Instagram, TikTok, YouTube, or similar apps, a brief prompt will appear before you continue."
        )
        Spacer(Modifier.height(12.dp))
        StepCard(
            number = "2",
            title = "Answer one quick question",
            body = "You'll be asked a short question — takes about 10 seconds. You can skip it anytime."
        )
        Spacer(Modifier.height(12.dp))
        StepCard(
            number = "3",
            title = "Rate your experience when done",
            body = "When you leave the app, you'll get a quick satisfaction rating (3 choices)."
        )
        Spacer(Modifier.height(12.dp))
        StepCard(
            number = "4",
            title = "5 PM & 9 PM daily check-ins",
            body = "Two short well-being surveys each day, sent as notifications."
        )

        Spacer(Modifier.height(32.dp))
        Text("Next: we'll show you exactly what the prompt looks like.",
            fontSize = 14.sp, color = MaterialTheme.colorScheme.secondary,
            textAlign = TextAlign.Center)
        Spacer(Modifier.height(20.dp))
        Button(onClick = onNext, modifier = Modifier.fillMaxWidth().height(52.dp)) {
            Text("Show me the prompt", fontSize = 16.sp)
        }
    }
}

@Composable
private fun StepCard(number: String, title: String, body: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.Top) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary),
                contentAlignment = Alignment.Center
            ) {
                Text(number, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
            }
            Spacer(Modifier.width(12.dp))
            Column {
                Text(title, fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
                Spacer(Modifier.height(4.dp))
                Text(body, fontSize = 13.sp, color = MaterialTheme.colorScheme.secondary,
                    lineHeight = 18.sp)
            }
        }
    }
}

// ── Page 1: App Selection ────────────────────────────────────────────────────

@Composable
private fun AppSelectionPage(onNext: (List<String>) -> Unit) {
    val selected = remember { mutableStateMapOf<String, Boolean>().apply {
        ALL_APPS.forEach { (pkg, _) -> put(pkg, true) }
    }}

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp)
    ) {
        Text("Which apps should we track?", fontSize = 22.sp, fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary)
        Spacer(Modifier.height(8.dp))
        Text("You'll receive prompts when you open the selected apps. Tap to toggle.",
            fontSize = 14.sp, color = MaterialTheme.colorScheme.secondary)
        Spacer(Modifier.height(24.dp))

        ALL_APPS.forEach { (pkg, name) ->
            val isOn = selected[pkg] == true
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 5.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (isOn) MaterialTheme.colorScheme.primaryContainer
                    else MaterialTheme.colorScheme.surface
                ),
                elevation = CardDefaults.cardElevation(if (isOn) 0.dp else 1.dp),
                onClick = { selected[pkg] = !isOn }
            ) {
                Row(
                    Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(name, fontSize = 15.sp, fontWeight = FontWeight.Medium,
                        modifier = Modifier.weight(1f))
                    Switch(
                        checked = isOn,
                        onCheckedChange = { selected[pkg] = it }
                    )
                }
            }
        }

        Spacer(Modifier.height(28.dp))
        Button(
            onClick = { onNext(selected.filter { it.value }.keys.toList()) },
            enabled = selected.values.any { it },
            modifier = Modifier.fillMaxWidth().height(52.dp)
        ) {
            val count = selected.values.count { it }
            Text("Continue with $count app${if (count != 1) "s" else ""}", fontSize = 16.sp)
        }
    }
}

// ── Page 2: Demo MRP ─────────────────────────────────────────────────────────

@Composable
private fun DemoMRPPage(
    text: String,
    onTextChange: (String) -> Unit,
    onNext: () -> Unit,
    onSkip: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
    ) {
        DemoBadge()
        Spacer(Modifier.height(12.dp))
        Text("Before you continue…", fontSize = 13.sp,
            color = MaterialTheme.colorScheme.secondary)
        Spacer(Modifier.height(8.dp))
        Text(
            "People use social media for different reasons at different moments. Right now, what are you hoping to do by using Instagram?",
            fontSize = 17.sp, fontWeight = FontWeight.Medium, lineHeight = 24.sp
        )
        Spacer(Modifier.height(6.dp))
        Text(
            "This is not about changing or limiting your use. It is just a brief moment of reflection.",
            fontSize = 13.sp, color = MaterialTheme.colorScheme.secondary
        )
        Spacer(Modifier.height(20.dp))
        OutlinedTextField(
            value = text,
            onValueChange = onTextChange,
            label = { Text("Your motivation…") },
            modifier = Modifier.fillMaxWidth().height(110.dp),
            maxLines = 4
        )
        Spacer(Modifier.weight(1f))
        Text("Try typing something, then tap Submit to see what happens next.",
            fontSize = 13.sp, color = MaterialTheme.colorScheme.secondary,
            textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
        Spacer(Modifier.height(12.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            OutlinedButton(onClick = onSkip, modifier = Modifier.weight(1f).height(52.dp)) {
                Text("Skip")
            }
            Button(
                onClick = onNext,
                enabled = text.isNotBlank(),
                modifier = Modifier.weight(1f).height(52.dp)
            ) {
                Text("Submit")
            }
        }
    }
}

// ── Page 2: Demo Satisfaction ────────────────────────────────────────────────

@Composable
private fun DemoSatisfactionPage(onNext: () -> Unit) {
    var selected by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        DemoBadge()
        Spacer(Modifier.height(12.dp))
        Text("Quick check-in", fontSize = 13.sp, color = MaterialTheme.colorScheme.secondary)
        Spacer(Modifier.height(8.dp))
        Text(
            "How satisfied are you with your experience on Instagram just now?",
            fontSize = 17.sp, fontWeight = FontWeight.Medium, lineHeight = 24.sp,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(24.dp))
        listOf("Not satisfied", "Neutral", "Satisfied").forEach { option ->
            OutlinedButton(
                onClick = { selected = option },
                modifier = Modifier.fillMaxWidth().padding(vertical = 5.dp).height(52.dp),
                colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = if (selected == option)
                        MaterialTheme.colorScheme.primaryContainer else Color.Transparent
                )
            ) {
                Text(option, fontSize = 15.sp,
                    fontWeight = if (selected == option) FontWeight.Bold else FontWeight.Normal)
            }
        }
        Spacer(Modifier.weight(1f))
        Text("This appears when you leave the social media app.",
            fontSize = 13.sp, color = MaterialTheme.colorScheme.secondary,
            textAlign = TextAlign.Center)
        Spacer(Modifier.height(12.dp))
        Button(
            onClick = onNext,
            enabled = selected != null,
            modifier = Modifier.fillMaxWidth().height(52.dp)
        ) {
            Text("Next: set up permissions", fontSize = 16.sp)
        }
    }
}

// ── Page 3: Accessibility Service Setup ──────────────────────────────────────

@Composable
private fun AccessibilitySetupPage(onNext: () -> Unit) {
    val context = LocalContext.current
    var granted by remember { mutableStateOf(isAccessibilityServiceEnabled(context)) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp)
    ) {
        Text("Step 1 of 2 — Accessibility Service",
            fontSize = 22.sp, fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary)
        Spacer(Modifier.height(8.dp))
        Text("This lets the app know when you open a social media app so it can show the prompt. No content from your apps is ever read or recorded.",
            fontSize = 14.sp, lineHeight = 20.sp)
        Spacer(Modifier.height(24.dp))

        InstructionStep(1, "Tap the button below to open Accessibility Settings.")
        Spacer(Modifier.height(10.dp))
        InstructionStep(2, "Find \"Downloaded apps\" or scroll to find \"SMU Study App Monitor\".")
        Spacer(Modifier.height(10.dp))
        InstructionStep(3, "Tap it and toggle it ON.")
        Spacer(Modifier.height(10.dp))
        InstructionStep(4, "Tap \"Allow\" on the confirmation dialog.")
        Spacer(Modifier.height(10.dp))
        InstructionStep(5, "Come back here and tap \"Check status\" below.")

        Spacer(Modifier.height(28.dp))

        StatusBanner(granted = granted, grantedText = "Accessibility service is ON",
            deniedText = "Not yet enabled")

        Spacer(Modifier.height(16.dp))

        if (!granted) {
            Button(
                onClick = { context.startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)) },
                modifier = Modifier.fillMaxWidth().height(52.dp)
            ) {
                Text("Open Accessibility Settings")
            }
            Spacer(Modifier.height(10.dp))
            OutlinedButton(
                onClick = { granted = isAccessibilityServiceEnabled(context) },
                modifier = Modifier.fillMaxWidth().height(52.dp)
            ) {
                Text("Check status")
            }
        } else {
            Button(onClick = onNext, modifier = Modifier.fillMaxWidth().height(52.dp)) {
                Text("Continue", fontSize = 16.sp)
            }
        }
    }
}

// ── Page 4: Overlay Permission Setup ─────────────────────────────────────────

@Composable
private fun OverlaySetupPage(onNext: () -> Unit) {
    val context = LocalContext.current
    var granted by remember { mutableStateOf(Settings.canDrawOverlays(context)) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp)
    ) {
        Text("Step 2 of 2 — Display Over Other Apps",
            fontSize = 22.sp, fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary)
        Spacer(Modifier.height(8.dp))
        Text("This allows the reflection prompt to appear on screen while you're using a social media app — like you just saw in the demo.",
            fontSize = 14.sp, lineHeight = 20.sp)
        Spacer(Modifier.height(24.dp))

        InstructionStep(1, "Tap the button below to open the permission screen.")
        Spacer(Modifier.height(10.dp))
        InstructionStep(2, "Find \"SMU Study App\" in the list.")
        Spacer(Modifier.height(10.dp))
        InstructionStep(3, "Toggle \"Allow display over other apps\" ON.")
        Spacer(Modifier.height(10.dp))
        InstructionStep(4, "Come back here and tap \"Check status\".")

        Spacer(Modifier.height(28.dp))

        StatusBanner(granted = granted, grantedText = "Overlay permission is ON",
            deniedText = "Not yet enabled")

        Spacer(Modifier.height(16.dp))

        if (!granted) {
            Button(
                onClick = {
                    context.startActivity(
                        Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                            Uri.parse("package:${context.packageName}"))
                    )
                },
                modifier = Modifier.fillMaxWidth().height(52.dp)
            ) {
                Text("Open Permission Settings")
            }
            Spacer(Modifier.height(10.dp))
            OutlinedButton(
                onClick = { granted = Settings.canDrawOverlays(context) },
                modifier = Modifier.fillMaxWidth().height(52.dp)
            ) {
                Text("Check status")
            }
        } else {
            Button(onClick = onNext, modifier = Modifier.fillMaxWidth().height(52.dp)) {
                Text("Continue", fontSize = 16.sp)
            }
        }
    }
}

// ── Page 5: All Set ───────────────────────────────────────────────────────────

@Composable
private fun AllSetPage(onGo: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(80.dp)
                .clip(CircleShape)
                .background(Color(0xFF4CAF50)),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Default.Check, contentDescription = null,
                tint = Color.White, modifier = Modifier.size(48.dp))
        }
        Spacer(Modifier.height(24.dp))
        Text("You're all set!", fontSize = 26.sp, fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary)
        Spacer(Modifier.height(16.dp))
        Text(
            "The study starts now. Open any social media app and you'll see the prompt just like in the demo.",
            fontSize = 15.sp, textAlign = TextAlign.Center, lineHeight = 22.sp
        )
        Spacer(Modifier.height(12.dp))
        Text(
            "You'll also receive notifications at 5:00 PM and 9:00 PM for your daily check-ins.",
            fontSize = 14.sp, textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.secondary, lineHeight = 20.sp
        )
        Spacer(Modifier.height(40.dp))
        Button(onClick = onGo, modifier = Modifier.fillMaxWidth().height(52.dp)) {
            Text("Go to Dashboard", fontSize = 16.sp)
        }
    }
}

// ── Shared components ─────────────────────────────────────────────────────────

@Composable
private fun DemoBadge() {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(4.dp))
            .background(MaterialTheme.colorScheme.primaryContainer)
            .padding(horizontal = 10.dp, vertical = 4.dp)
    ) {
        Text("DEMO PREVIEW", fontSize = 11.sp,
            color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun InstructionStep(number: Int, text: String) {
    Row(verticalAlignment = Alignment.Top) {
        Box(
            modifier = Modifier
                .size(26.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primaryContainer),
            contentAlignment = Alignment.Center
        ) {
            Text("$number", fontSize = 13.sp, fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary)
        }
        Spacer(Modifier.width(12.dp))
        Text(text, fontSize = 14.sp, lineHeight = 20.sp,
            modifier = Modifier.padding(top = 3.dp))
    }
}

@Composable
private fun StatusBanner(granted: Boolean, grantedText: String, deniedText: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (granted) Color(0xFFE8F5E9) else Color(0xFFFFF3E0)
        )
    ) {
        Row(
            Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = if (granted) Icons.Default.Check else Icons.Default.Close,
                contentDescription = null,
                tint = if (granted) Color(0xFF4CAF50) else Color(0xFFF57C00),
                modifier = Modifier.size(24.dp)
            )
            Spacer(Modifier.width(12.dp))
            Text(
                if (granted) grantedText else deniedText,
                fontWeight = FontWeight.SemiBold,
                color = if (granted) Color(0xFF2E7D32) else Color(0xFFE65100)
            )
        }
    }
}

private fun isAccessibilityServiceEnabled(context: Context): Boolean {
    val am = context.getSystemService(Context.ACCESSIBILITY_SERVICE) as AccessibilityManager
    return am.getEnabledAccessibilityServiceList(AccessibilityServiceInfo.FEEDBACK_ALL_MASK)
        .any { it.resolveInfo.serviceInfo.packageName == context.packageName }
}
