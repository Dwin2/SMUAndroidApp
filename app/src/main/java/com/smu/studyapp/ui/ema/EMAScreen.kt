package com.smu.studyapp.ui.ema

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EMAScreen(surveyType: String, vm: EMAViewModel, onDone: () -> Unit) {
    val timeLabel = if (surveyType == "EMA_5PM") "5 PM" else "9 PM"
    val periodLabel = if (surveyType == "EMA_5PM") "today" else "this evening"

    val responses = remember { mutableStateMapOf<String, String>() }

    // SMU experience items (1-5 scale)
    val smuItems = listOf(
        "smu_happy" to "My social media use felt happy",
        "smu_meaningful" to "My social media use felt meaningful",
        "smu_effortful" to "My social media use felt effortful"
    )
    val likertLabels = listOf("1", "2", "3", "4", "5")

    // Well-being items (0-100 slider)
    val wbItems = listOf(
        "wb_happy" to "Happy",
        "wb_inspired" to "Inspired",
        "wb_satisfied" to "Life satisfaction",
        "wb_sad" to "Sad",
        "wb_angry" to "Angry",
        "wb_lonely" to "Lonely"
    )

    val wbSliders = remember { mutableStateMapOf<String, Float>() }

    val isComplete = smuItems.all { responses.containsKey(it.first) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("$timeLabel Survey") },
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
            // SMU Experience section [4.1]
            Text("Social Media Use $periodLabel", fontWeight = FontWeight.Bold, fontSize = 17.sp,
                color = MaterialTheme.colorScheme.primary)
            Text("Thinking about your social media use $periodLabel, please rate:",
                fontSize = 14.sp, color = MaterialTheme.colorScheme.secondary)
            Spacer(Modifier.height(12.dp))

            smuItems.forEach { (key, label) ->
                Text(label, fontWeight = FontWeight.Medium, fontSize = 15.sp)
                Spacer(Modifier.height(4.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    likertLabels.forEach { opt ->
                        val selected = responses[key] == opt
                        FilterChip(
                            selected = selected,
                            onClick = { responses[key] = opt },
                            label = { Text(opt) }
                        )
                    }
                }
                Text("1 = strongly disagree   5 = strongly agree",
                    fontSize = 11.sp, color = MaterialTheme.colorScheme.secondary)
                Spacer(Modifier.height(16.dp))
            }

            Divider()
            Spacer(Modifier.height(16.dp))

            // Well-being section [4.2]
            Text("How are you feeling right now?", fontWeight = FontWeight.Bold, fontSize = 17.sp,
                color = MaterialTheme.colorScheme.primary)
            Text("On a scale from 0 to 100:", fontSize = 14.sp,
                color = MaterialTheme.colorScheme.secondary)
            Spacer(Modifier.height(12.dp))

            wbItems.forEach { (key, label) ->
                val sliderVal = wbSliders.getOrDefault(key, 50f)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(label, modifier = Modifier.width(120.dp), fontSize = 15.sp,
                        fontWeight = FontWeight.Medium)
                    Slider(
                        value = sliderVal,
                        onValueChange = {
                            wbSliders[key] = it
                            responses[key] = it.toInt().toString()
                        },
                        valueRange = 0f..100f,
                        steps = 99,
                        modifier = Modifier.weight(1f)
                    )
                    Text("${sliderVal.toInt()}", modifier = Modifier.width(36.dp),
                        fontSize = 14.sp, color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold)
                }
                Spacer(Modifier.height(8.dp))
            }

            Spacer(Modifier.height(24.dp))
            Button(
                onClick = {
                    // Ensure all WB sliders have a value
                    wbItems.forEach { (key, _) ->
                        if (!responses.containsKey(key))
                            responses[key] = wbSliders.getOrDefault(key, 50f).toInt().toString()
                    }
                    vm.submit(responses.toMap()) { onDone() }
                },
                enabled = isComplete,
                modifier = Modifier.fillMaxWidth().height(52.dp)
            ) {
                Text("Submit Survey", fontSize = 16.sp)
            }
            Spacer(Modifier.height(24.dp))
        }
    }
}
