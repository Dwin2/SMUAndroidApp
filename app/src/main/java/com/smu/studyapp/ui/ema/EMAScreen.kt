package com.smu.studyapp.ui.ema

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EMAScreen(surveyType: String, vm: EMAViewModel, onDone: () -> Unit) {
    val timeLabel = if (surveyType == "EMA_5PM") "5 PM" else "9 PM"
    val periodLabel = if (surveyType == "EMA_5PM") "today" else "this evening"

    val responses = remember { mutableStateMapOf<String, String>() }

    // SMU Experience (1–5 Likert)
    val smuItems = listOf(
        "smu_happy" to "My social media use felt happy.",
        "smu_meaningful" to "My social media use felt meaningful.",
        "smu_effortful" to "My social media use felt effortful."
    )
    val likertOptions = listOf("1", "2", "3", "4", "5")

    // Subjective Well-Being (0–100 VAS)
    val wbItems = listOf(
        "wb_happy" to "Happy",
        "wb_inspired" to "Inspired",
        "wb_satisfied" to "Life satisfaction",
        "wb_sad" to "Sad",
        "wb_angry" to "Angry",
        "wb_lonely" to "Lonely"
    )
    val wbSliders = remember { mutableStateMapOf<String, Float>() }
    val wbTouched = remember { mutableStateMapOf<String, Boolean>() }

    val smuComplete = smuItems.all { responses.containsKey(it.first) }
    val wbComplete = wbItems.all { wbTouched[it.first] == true }
    val isComplete = smuComplete && wbComplete

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("$timeLabel Check-in") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = androidx.compose.ui.graphics.Color.White
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(20.dp)
        ) {
            // ── SMU Experience ─────────────────────────────────────────
            Text(
                "Social Media Use",
                fontWeight = FontWeight.Bold, fontSize = 18.sp,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(Modifier.height(4.dp))
            Text(
                "Thinking about your social media use $periodLabel, please rate the following statements.",
                fontSize = 14.sp, lineHeight = 20.sp
            )
            Spacer(Modifier.height(8.dp))
            Text(
                "1 = Strongly disagree   →   5 = Strongly agree",
                fontSize = 12.sp, color = MaterialTheme.colorScheme.secondary
            )
            Spacer(Modifier.height(16.dp))

            smuItems.forEach { (key, label) ->
                Text(label, fontWeight = FontWeight.Medium, fontSize = 15.sp)
                Spacer(Modifier.height(6.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    likertOptions.forEach { opt ->
                        val selected = responses[key] == opt
                        FilterChip(
                            selected = selected,
                            onClick = { responses[key] = opt },
                            label = { Text(opt) }
                        )
                    }
                }
                Spacer(Modifier.height(16.dp))
            }

            HorizontalDivider()
            Spacer(Modifier.height(16.dp))

            // ── Subjective Well-Being ─────────────────────────────────
            Text(
                "How are you feeling right now?",
                fontWeight = FontWeight.Bold, fontSize = 18.sp,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(Modifier.height(4.dp))
            Text(
                "On a scale from 0 to 100, how strongly are you feeling the following emotions right now?",
                fontSize = 14.sp, lineHeight = 20.sp
            )
            Spacer(Modifier.height(8.dp))
            Text(
                "0 = not at all   →   100 = extremely",
                fontSize = 12.sp, color = MaterialTheme.colorScheme.secondary
            )
            Spacer(Modifier.height(16.dp))

            wbItems.forEach { (key, label) ->
                val touched = wbTouched[key] == true
                val sliderVal = wbSliders.getOrDefault(key, 50f)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(label, modifier = Modifier.width(120.dp), fontSize = 15.sp,
                        fontWeight = FontWeight.Medium)
                    Slider(
                        value = sliderVal,
                        onValueChange = {
                            wbSliders[key] = it
                            wbTouched[key] = true
                            responses[key] = it.toInt().toString()
                        },
                        valueRange = 0f..100f,
                        steps = 99,
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        if (touched) "${sliderVal.toInt()}" else "—",
                        modifier = Modifier.width(40.dp),
                        fontSize = 14.sp,
                        color = if (touched) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.secondary,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(Modifier.height(8.dp))
            }

            Spacer(Modifier.height(20.dp))
            if (!isComplete) {
                Text(
                    when {
                        !smuComplete && !wbComplete -> "Please complete both sections."
                        !smuComplete -> "Please rate all three statements above."
                        else -> "Please move every slider to confirm your rating."
                    },
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.error
                )
                Spacer(Modifier.height(8.dp))
            }
            Button(
                onClick = { vm.submit(responses.toMap()) { onDone() } },
                enabled = isComplete,
                modifier = Modifier.fillMaxWidth().height(52.dp)
            ) {
                Text("Submit", fontSize = 16.sp)
            }
            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
fun ExpiredScreen(onDone: () -> Unit) {
    Box(
        modifier = Modifier.fillMaxSize().padding(28.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                "This check-in has expired",
                fontSize = 22.sp, fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(Modifier.height(12.dp))
            Text(
                "Each check-in is open for 3 hours. Don't worry — your next check-in will arrive on schedule.",
                fontSize = 15.sp, lineHeight = 22.sp,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(28.dp))
            Button(
                onClick = onDone,
                modifier = Modifier.fillMaxWidth().height(52.dp)
            ) {
                Text("OK", fontSize = 16.sp)
            }
        }
    }
}
