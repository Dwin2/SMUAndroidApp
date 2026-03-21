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
import com.smu.studyapp.utils.SamplingManager

@Composable
fun PromptScreen(
    sessionId: String,
    appPackage: String,
    promptType: String,
    vm: PromptViewModel,
    onDismiss: () -> Unit
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
                "T" -> MRPContent(appPackage, vm, onDismiss)
                "C" -> NeutralPromptContent(appPackage, vm, onDismiss)
                "SATISFACTION" -> SatisfactionContent(appPackage, vm, onDismiss)
            }
        }
    }
}

@Composable
private fun MRPContent(appPackage: String, vm: PromptViewModel, onDismiss: () -> Unit) {
    var text by remember { mutableStateOf("") }
    val appName = SamplingManager.getAppName(appPackage)

    Column(Modifier.padding(24.dp)) {
        Text("Before you continue…", fontSize = 13.sp,
            color = MaterialTheme.colorScheme.secondary, fontWeight = FontWeight.Medium)
        Spacer(Modifier.height(8.dp))
        Text(
            "People use social media for different reasons at different moments. Right now, what are you hoping to do by using $appName?",
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
            modifier = Modifier
                .fillMaxWidth()
                .height(100.dp),
            maxLines = 4
        )
        Spacer(Modifier.height(20.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            OutlinedButton(onClick = onDismiss, modifier = Modifier.weight(1f)) {
                Text("Skip")
            }
            Button(
                onClick = { vm.submitMRP(text) { onDismiss() } },
                enabled = text.isNotBlank(),
                modifier = Modifier.weight(1f)
            ) {
                Text("Submit")
            }
        }
    }
}

private val neutralQuestions = listOf(
    "What is one object you can see in front of you right now?",
    "What is one object you can see near you right now?",
    "Where are you sitting or standing right now?",
    "What colors are most noticeable around you right now?",
    "What object do your eyes naturally fall on right now?"
)

@Composable
private fun NeutralPromptContent(appPackage: String, vm: PromptViewModel, onDismiss: () -> Unit) {
    var text by remember { mutableStateOf("") }
    val question = remember { neutralQuestions.random() }

    Column(Modifier.padding(24.dp)) {
        Text("Take a moment to notice your surroundings.",
            fontSize = 13.sp, color = MaterialTheme.colorScheme.secondary)
        Spacer(Modifier.height(8.dp))
        Text(question, fontSize = 16.sp, fontWeight = FontWeight.Medium, lineHeight = 22.sp)
        Spacer(Modifier.height(16.dp))
        OutlinedTextField(
            value = text, onValueChange = { text = it },
            label = { Text("Your answer…") },
            modifier = Modifier
                .fillMaxWidth()
                .height(100.dp),
            maxLines = 4
        )
        Spacer(Modifier.height(20.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            OutlinedButton(onClick = onDismiss, modifier = Modifier.weight(1f)) {
                Text("Skip")
            }
            Button(
                onClick = { vm.submitNP(text, question) { onDismiss() } },
                enabled = text.isNotBlank(),
                modifier = Modifier.weight(1f)
            ) {
                Text("Submit")
            }
        }
    }
}

@Composable
private fun SatisfactionContent(appPackage: String, vm: PromptViewModel, onDismiss: () -> Unit) {
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
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            OutlinedButton(onClick = onDismiss, modifier = Modifier.weight(1f)) {
                Text("Skip")
            }
            Button(
                onClick = { vm.submitSatisfaction(selected!!) { onDismiss() } },
                enabled = selected != null,
                modifier = Modifier.weight(1f)
            ) {
                Text("Submit")
            }
        }
    }
}
