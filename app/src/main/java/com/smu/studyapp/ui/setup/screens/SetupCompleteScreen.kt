package com.smu.studyapp.ui.setup.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun SetupCompleteScreen(onGoDashboard: () -> Unit) {
    var showDemo by remember { mutableStateOf(false) }

    if (showDemo) {
        DemoPromptOverlay(onDismiss = { showDemo = false })
        return
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.height(16.dp))
        Icon(Icons.Default.CheckCircle, contentDescription = null,
            tint = Color(0xFF4CAF50), modifier = Modifier.size(72.dp))
        Spacer(Modifier.height(24.dp))
        Text("You're all set!", fontSize = 26.sp, fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary)
        Spacer(Modifier.height(16.dp))
        Text(
            "The study starts now. You'll see brief prompts when you open Instagram, TikTok, YouTube, and other social media apps.\n\nYou'll also receive 5 PM and 9 PM survey reminders each day.",
            fontSize = 15.sp, textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(32.dp))

        // Demo section
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer
            )
        ) {
            Column(
                Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("Want to see what the prompt looks like?",
                    fontWeight = FontWeight.SemiBold, fontSize = 15.sp,
                    textAlign = TextAlign.Center)
                Spacer(Modifier.height(12.dp))
                OutlinedButton(onClick = { showDemo = true }) {
                    Text("Preview the prompt")
                }
            }
        }

        Spacer(Modifier.height(32.dp))
        Button(
            onClick = onGoDashboard,
            modifier = Modifier.fillMaxWidth().height(52.dp)
        ) {
            Text("Go to Dashboard", fontSize = 16.sp)
        }
    }
}

@Composable
private fun DemoPromptOverlay(onDismiss: () -> Unit) {
    var text by remember { mutableStateOf("") }
    var submitted by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        if (!submitted) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(12.dp)
            ) {
                Column(Modifier.padding(24.dp)) {
                    Text("DEMO — this is what participants see",
                        fontSize = 11.sp, color = MaterialTheme.colorScheme.secondary,
                        fontWeight = FontWeight.SemiBold)
                    Spacer(Modifier.height(8.dp))
                    Text("Before you continue…", fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.secondary)
                    Spacer(Modifier.height(6.dp))
                    Text(
                        "People use social media for different reasons at different moments. Right now, what are you hoping to do by using Instagram?",
                        fontSize = 16.sp, fontWeight = FontWeight.Medium, lineHeight = 22.sp
                    )
                    Text(
                        "This is not about changing or limiting your use. It is just a brief moment of reflection.",
                        fontSize = 13.sp, color = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.padding(top = 6.dp)
                    )
                    Spacer(Modifier.height(16.dp))
                    OutlinedTextField(
                        value = text, onValueChange = { text = it },
                        label = { Text("Your motivation…") },
                        modifier = Modifier.fillMaxWidth().height(100.dp),
                        maxLines = 4
                    )
                    Spacer(Modifier.height(20.dp))
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        OutlinedButton(onClick = onDismiss, modifier = Modifier.weight(1f)) {
                            Text("Skip")
                        }
                        Button(
                            onClick = { submitted = true },
                            enabled = text.isNotBlank(),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Submit")
                        }
                    }
                }
            }
        } else {
            // Show satisfaction survey after "submitting"
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(12.dp)
            ) {
                var selected by remember { mutableStateOf<String?>(null) }
                Column(Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("DEMO — shown when you close the app",
                        fontSize = 11.sp, color = MaterialTheme.colorScheme.secondary,
                        fontWeight = FontWeight.SemiBold)
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "How satisfied are you with your experience on Instagram just now?",
                        fontSize = 16.sp, fontWeight = FontWeight.Medium, lineHeight = 22.sp,
                        textAlign = TextAlign.Center
                    )
                    Spacer(Modifier.height(16.dp))
                    listOf("Not satisfied", "Neutral", "Satisfied").forEach { option ->
                        OutlinedButton(
                            onClick = { selected = option },
                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                            colors = ButtonDefaults.outlinedButtonColors(
                                containerColor = if (selected == option)
                                    MaterialTheme.colorScheme.primaryContainer
                                else Color.Transparent
                            )
                        ) { Text(option) }
                    }
                    Spacer(Modifier.height(16.dp))
                    Button(
                        onClick = onDismiss,
                        enabled = selected != null,
                        modifier = Modifier.fillMaxWidth()
                    ) { Text("Done") }
                }
            }
        }
    }
}
