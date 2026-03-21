package com.smu.studyapp.ui.setup.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val baselineQuestions = listOf(
    SurveyQuestion(
        key = "mindset_awareness",
        text = "I am aware of why I use social media in any given moment.",
        options = listOf("1 - Strongly disagree", "2", "3", "4", "5 - Strongly agree"),
        section = "SMU Mindset"
    ),
    SurveyQuestion(
        key = "wb_happy",
        text = "On a scale from 1–5, how happy are you feeling right now?",
        options = listOf("1 - Not at all", "2", "3", "4", "5 - Extremely"),
        section = "Well-being"
    ),
    SurveyQuestion(
        key = "smu_exp_happy",
        text = "My social media use generally feels happy.",
        options = listOf("1 - Strongly disagree", "2", "3", "4", "5 - Strongly agree"),
        section = "SMU Experience"
    )
)

data class SurveyQuestion(
    val key: String,
    val text: String,
    val options: List<String>,
    val section: String? = null
)

@Composable
fun BaselineSurveyScreen(onSubmit: (Map<String, String>) -> Unit) {
    val responses = remember { mutableStateMapOf<String, String>() }
    var currentPage by remember { mutableIntStateOf(0) }
    val question = baselineQuestions[currentPage]
    val isLast = currentPage == baselineQuestions.lastIndex

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
    ) {
        Text("Baseline Survey", fontSize = 24.sp, fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary)
        Text("Step 2 of 3  ·  Question ${currentPage + 1} of ${baselineQuestions.size}",
            fontSize = 13.sp, color = MaterialTheme.colorScheme.secondary)

        Spacer(Modifier.height(32.dp))

        question.section?.let {
            Text(it, fontSize = 12.sp, color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(6.dp))
        }

        Text(question.text, fontSize = 17.sp, fontWeight = FontWeight.Medium, lineHeight = 24.sp)

        Spacer(Modifier.height(24.dp))

        question.options.forEach { option ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                RadioButton(
                    selected = responses[question.key] == option,
                    onClick = { responses[question.key] = option }
                )
                Spacer(Modifier.width(8.dp))
                Text(option, fontSize = 15.sp)
            }
        }

        Spacer(Modifier.weight(1f))

        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            if (currentPage > 0) {
                OutlinedButton(
                    onClick = { currentPage-- },
                    modifier = Modifier.weight(1f).height(52.dp)
                ) {
                    Text("Back")
                }
            }
            Button(
                onClick = {
                    if (isLast) onSubmit(responses.toMap())
                    else currentPage++
                },
                enabled = responses.containsKey(question.key),
                modifier = Modifier.weight(1f).height(52.dp)
            ) {
                Text(if (isLast) "Submit" else "Next", fontSize = 16.sp)
            }
        }
    }
}
