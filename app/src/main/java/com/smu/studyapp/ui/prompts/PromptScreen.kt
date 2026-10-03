package com.smu.studyapp.ui.prompts

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.smu.studyapp.utils.NudgeManager
import com.smu.studyapp.utils.SamplingManager

// Nudge-mode notes, shown above the standard MRP/NP question (additive — they don't replace
// the standard preamble). Days 1–3 are warmer/longer while participants learn the system.
private const val NUDGE_NOTE_DAYS_1_3 =
    "We'd love to hear about your experience. Just a sentence or two makes a real difference, " +
        "and we'll send at most 15 prompts a day. If now isn't the right moment, tap Skip for now " +
        "and the app will open as usual."
private const val NUDGE_NOTE_DAYS_4_7 =
    "Your response matters to us. If now isn't the right moment, tap Skip for now and the app " +
        "will open as usual. Otherwise, thanks for taking a moment."

@Composable
fun PromptScreen(
    sessionId: String,
    appPackage: String,
    promptType: String,
    vm: PromptViewModel,
    onSubmitDone: () -> Unit,
    onSkip: () -> Unit,
    mode: String = NudgeManager.MODE_STANDARD,
    studyDay: Int = 1
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .wrapContentHeight(),
            shape = RoundedCornerShape(20.dp),
            elevation = CardDefaults.cardElevation(12.dp)
        ) {
            when (promptType) {
                "T" -> MRPContent(appPackage, vm, onSubmitDone, onSkip, mode, studyDay)
                "C" -> NeutralPromptContent(vm, onSubmitDone, onSkip, mode, studyDay)
                "SATISFACTION" -> SatisfactionContent(appPackage, vm, onSubmitDone, onSkip)
            }
        }
    }
}

/** Warm note rendered above the question when the prompt is in nudge mode. */
@Composable
private fun NudgeNote(studyDay: Int) {
    val note = if (studyDay <= 3) NUDGE_NOTE_DAYS_1_3 else NUDGE_NOTE_DAYS_4_7
    Surface(
        color = MaterialTheme.colorScheme.primaryContainer,
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            note,
            fontSize = 13.sp,
            lineHeight = 18.sp,
            color = MaterialTheme.colorScheme.onPrimaryContainer,
            modifier = Modifier.padding(12.dp)
        )
    }
    Spacer(Modifier.height(16.dp))
}

private fun skipLabel(mode: String) =
    if (mode == NudgeManager.MODE_NUDGE) "Skip for now" else "Skip"

@Composable
private fun MRPContent(
    appPackage: String,
    vm: PromptViewModel,
    onSubmitDone: () -> Unit,
    onSkip: () -> Unit,
    mode: String,
    studyDay: Int
) {
    var text by remember { mutableStateOf("") }
    val appName = SamplingManager.getAppName(appPackage)

    Column(Modifier.padding(24.dp)) {
        if (mode == NudgeManager.MODE_NUDGE) NudgeNote(studyDay)
        Text(
            "People use it to achieve many different goals.",
            fontSize = 13.sp, color = MaterialTheme.colorScheme.secondary, lineHeight = 18.sp
        )
        Spacer(Modifier.height(16.dp))
        Text(
            "In a sentence or two, what are you hoping to do on $appName right now?",
            fontSize = 16.sp, fontWeight = FontWeight.Medium, lineHeight = 22.sp
        )
        Spacer(Modifier.height(8.dp))
        Text(
            "Note: We're not asking you to limit your use. Just to take a moment to reflect on your intention.",
            fontSize = 12.sp, color = MaterialTheme.colorScheme.secondary, lineHeight = 16.sp
        )
        Spacer(Modifier.height(16.dp))
        OutlinedTextField(
            value = text, onValueChange = { text = it },
            label = { Text("Your answer…") },
            modifier = Modifier
                .fillMaxWidth()
                .height(110.dp),
            maxLines = 4
        )
        Spacer(Modifier.height(20.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            OutlinedButton(onClick = onSkip, modifier = Modifier.weight(1f)) {
                Text(skipLabel(mode))
            }
            Button(
                onClick = { vm.submitMRP(text) { onSubmitDone() } },
                enabled = text.isNotBlank(),
                modifier = Modifier.weight(1f)
            ) {
                Text("Submit")
            }
        }
    }
}

@Composable
private fun NeutralPromptContent(
    vm: PromptViewModel,
    onSubmitDone: () -> Unit,
    onSkip: () -> Unit,
    mode: String,
    studyDay: Int
) {
    var text by remember { mutableStateOf("") }
    val question = "Take a moment to look at your surroundings. In a sentence or two, describe one object or detail that catches your eye right now."

    Column(Modifier.padding(24.dp)) {
        if (mode == NudgeManager.MODE_NUDGE) NudgeNote(studyDay)
        Text(
            question,
            fontSize = 16.sp, fontWeight = FontWeight.Medium, lineHeight = 22.sp
        )
        Spacer(Modifier.height(16.dp))
        OutlinedTextField(
            value = text, onValueChange = { text = it },
            label = { Text("Your answer…") },
            modifier = Modifier
                .fillMaxWidth()
                .height(110.dp),
            maxLines = 4
        )
        Spacer(Modifier.height(20.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            OutlinedButton(onClick = onSkip, modifier = Modifier.weight(1f)) {
                Text(skipLabel(mode))
            }
            Button(
                onClick = { vm.submitNP(text, question) { onSubmitDone() } },
                enabled = text.isNotBlank(),
                modifier = Modifier.weight(1f)
            ) {
                Text("Submit")
            }
        }
    }
}

@Composable
private fun SatisfactionContent(
    appPackage: String,
    vm: PromptViewModel,
    onSubmitDone: () -> Unit,
    onSkip: () -> Unit
) {
    var selected by remember { mutableStateOf<String?>(null) }
    val appName = SamplingManager.getAppName(appPackage)
    val options = listOf("Not satisfied", "Neutral", "Satisfied")

    Column(Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Text("Quick check-in", fontSize = 13.sp, color = MaterialTheme.colorScheme.secondary)
        Spacer(Modifier.height(8.dp))
        Text(
            "How satisfied are you with your experience on $appName just now?",
            fontSize = 16.sp, fontWeight = FontWeight.Medium, lineHeight = 22.sp,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(20.dp))
        options.forEach { option ->
            val isSelected = selected == option
            OutlinedButton(
                onClick = { selected = option },
                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer
                    else Color.Transparent
                )
            ) {
                Text(option, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal)
            }
        }
        Spacer(Modifier.height(20.dp))
        Button(
            onClick = { vm.submitSatisfaction(selected!!) { onSubmitDone() } },
            enabled = selected != null,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Submit")
        }
    }
}
