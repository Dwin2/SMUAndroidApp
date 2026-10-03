package com.smu.studyapp.ui.setup.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Setup-flow mock of the real opening prompt. Lets the participant feel what a pop-up
 * is like before the study actually starts. Nothing is saved here — the response is
 * discarded; this is purely for familiarization.
 */
@Composable
fun MockPromptScreen(studyGroup: String, onNext: () -> Unit) {
    var text by remember { mutableStateOf("") }
    var submitted by remember { mutableStateOf(false) }
    val isTreatment = studyGroup == "T"

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp)
    ) {
        Text(
            "Try a sample prompt",
            fontSize = 22.sp, fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(Modifier.height(8.dp))
        Text(
            "Here's what a real opening prompt will look like. Give it a quick try — " +
                "your response on this screen is not recorded.",
            fontSize = 14.sp, lineHeight = 20.sp
        )
        Spacer(Modifier.height(20.dp))

        // Mock prompt card mirrors the real overlay's MRP card.
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            elevation = CardDefaults.cardElevation(6.dp)
        ) {
            Column(Modifier.padding(20.dp)) {
                Text(
                    "Practice prompt",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(Modifier.height(10.dp))
                if (isTreatment) {
                    Text(
                        "People use it to achieve many different goals.",
                        fontSize = 13.sp, color = MaterialTheme.colorScheme.secondary, lineHeight = 18.sp
                    )
                    Spacer(Modifier.height(12.dp))
                    Text(
                        "In a sentence or two, what are you hoping to do on Instagram right now?",
                        fontSize = 16.sp, fontWeight = FontWeight.Medium, lineHeight = 22.sp
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "Note: We're not asking you to limit your use. Just to take a moment to reflect on your intention.",
                        fontSize = 12.sp, color = MaterialTheme.colorScheme.secondary, lineHeight = 16.sp
                    )
                } else {
                    Text(
                        "Take a moment to look at your surroundings. In a sentence or two, " +
                            "describe one object or detail that catches your eye right now.",
                        fontSize = 16.sp, fontWeight = FontWeight.Medium, lineHeight = 22.sp
                    )
                }
                Spacer(Modifier.height(16.dp))
                OutlinedTextField(
                    value = text,
                    onValueChange = { text = it; submitted = false },
                    label = { Text("Your answer…") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(110.dp),
                    maxLines = 4,
                    enabled = !submitted
                )
                Spacer(Modifier.height(16.dp))
                Button(
                    onClick = { submitted = true },
                    enabled = text.isNotBlank() && !submitted,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(if (submitted) "Nice — that's exactly how it works." else "Submit")
                }
            }
        }

        Spacer(Modifier.height(20.dp))
        if (submitted) {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFFE8F5E9)
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    "That's the whole flow. During the study, this pop-up will appear over " +
                        "the social media app you opened, and a short close-out question will " +
                        "appear when you finish your session.",
                    fontSize = 13.sp, lineHeight = 18.sp,
                    color = Color(0xFF1B5E20),
                    modifier = Modifier.padding(14.dp)
                )
            }
        }

        Spacer(Modifier.height(24.dp))
        Button(
            onClick = onNext,
            enabled = submitted,
            modifier = Modifier.fillMaxWidth().height(52.dp)
        ) {
            Text(if (submitted) "Continue" else "Try the sample first", fontSize = 16.sp)
        }
        Spacer(Modifier.height(20.dp))
    }
}
