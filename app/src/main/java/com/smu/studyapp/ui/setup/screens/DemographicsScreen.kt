package com.smu.studyapp.ui.setup.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DemographicsScreen(
    onNext: (name: String, age: Int, gender: String, code: String, windowStart: Int, windowEnd: Int) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var age by remember { mutableStateOf("") }
    var gender by remember { mutableStateOf("") }
    var participantCode by remember { mutableStateOf("") }
    var windowStart by remember { mutableStateOf("8") }
    var windowEnd by remember { mutableStateOf("23") }
    var genderExpanded by remember { mutableStateOf(false) }

    val genderOptions = listOf("Male", "Female", "Non-binary", "Prefer not to say")

    val isValid = name.isNotBlank() && age.toIntOrNull() != null &&
            gender.isNotBlank() && participantCode.isNotBlank() &&
            windowStart.toIntOrNull() in 0..23 && windowEnd.toIntOrNull() in 0..23

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp)
    ) {
        Text("About You", fontSize = 24.sp, fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary)
        Text("Step 1 of 3", fontSize = 13.sp, color = MaterialTheme.colorScheme.secondary)
        Spacer(Modifier.height(24.dp))

        OutlinedTextField(
            value = name, onValueChange = { name = it },
            label = { Text("First name") },
            modifier = Modifier.fillMaxWidth(), singleLine = true
        )
        Spacer(Modifier.height(12.dp))

        OutlinedTextField(
            value = participantCode, onValueChange = { participantCode = it },
            label = { Text("Participant ID (provided by researcher)") },
            modifier = Modifier.fillMaxWidth(), singleLine = true
        )
        Spacer(Modifier.height(12.dp))

        OutlinedTextField(
            value = age, onValueChange = { age = it.filter { c -> c.isDigit() } },
            label = { Text("Age") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth(), singleLine = true
        )
        Spacer(Modifier.height(12.dp))

        ExposedDropdownMenuBox(expanded = genderExpanded, onExpandedChange = { genderExpanded = it }) {
            OutlinedTextField(
                value = gender, onValueChange = {},
                label = { Text("Gender") },
                readOnly = true,
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = genderExpanded) },
                modifier = Modifier.fillMaxWidth().menuAnchor()
            )
            ExposedDropdownMenu(expanded = genderExpanded, onDismissRequest = { genderExpanded = false }) {
                genderOptions.forEach {
                    DropdownMenuItem(text = { Text(it) }, onClick = { gender = it; genderExpanded = false })
                }
            }
        }
        Spacer(Modifier.height(24.dp))

        Text("Sampling Window", fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
        Text("We'll only prompt you within this daily window (24h format).",
            fontSize = 13.sp, color = MaterialTheme.colorScheme.secondary)
        Spacer(Modifier.height(8.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            OutlinedTextField(
                value = windowStart,
                onValueChange = { windowStart = it.filter { c -> c.isDigit() } },
                label = { Text("Start hour (0-23)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.weight(1f), singleLine = true
            )
            OutlinedTextField(
                value = windowEnd,
                onValueChange = { windowEnd = it.filter { c -> c.isDigit() } },
                label = { Text("End hour (0-23)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.weight(1f), singleLine = true
            )
        }
        Spacer(Modifier.height(32.dp))

        Button(
            onClick = {
                onNext(name.trim(), age.toInt(), gender, participantCode.trim(),
                    windowStart.toInt(), windowEnd.toInt())
            },
            enabled = isValid,
            modifier = Modifier.fillMaxWidth().height(52.dp)
        ) {
            Text("Continue", fontSize = 16.sp)
        }
    }
}
