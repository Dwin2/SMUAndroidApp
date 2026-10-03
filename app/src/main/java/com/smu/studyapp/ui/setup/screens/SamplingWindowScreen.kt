package com.smu.studyapp.ui.setup.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.smu.studyapp.utils.SamplingManager

@Composable
fun SamplingWindowScreen(
    initialStartMin: Int = 8 * 60,
    initialEndMin: Int = 23 * 60,
    initialSelectedApps: List<String> = emptyList(),
    onNext: (startMin: Int, endMin: Int, selectedApps: List<String>) -> Unit
) {
    val context = LocalContext.current
    var startMin by remember { mutableIntStateOf(initialStartMin) }
    var endMin by remember { mutableIntStateOf(initialEndMin) }
    // Auto-detected installed apps. No deselect — whatever you have installed is tracked.
    val installedPackages = remember { SamplingManager.installedTargetPackages(context) }
    val installedNames = remember { SamplingManager.installedTargetNames(context) }
    val isWindowValid = SamplingManager.isValidWindow(startMin, endMin)
    val anyAppInstalled = installedPackages.isNotEmpty()
    val canContinue = isWindowValid && anyAppInstalled

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp)
    ) {
        Text(
            "When should we reach you?",
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(Modifier.height(12.dp))
        Text(
            "We'll only send you prompts during your active hours. The default window is 8:00 AM – 11:00 PM. Feel free to adjust it to match your schedule.",
            fontSize = 14.sp,
            lineHeight = 20.sp
        )
        Spacer(Modifier.height(20.dp))

        TimeDropdown(
            label = "Start time",
            minutesOfDay = startMin,
            onChange = { startMin = it }
        )
        Spacer(Modifier.height(12.dp))
        TimeDropdown(
            label = "End time",
            minutesOfDay = endMin,
            onChange = { endMin = it }
        )

        Spacer(Modifier.height(8.dp))
        val lengthHrs = SamplingManager.windowLengthMinutes(startMin, endMin) / 60
        val lengthMins = SamplingManager.windowLengthMinutes(startMin, endMin) % 60
        Text(
            "Window length: ${lengthHrs}h ${lengthMins}m",
            fontSize = 13.sp,
            color = if (isWindowValid) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.error
        )
        if (!isWindowValid) {
            Text(
                "The window must be at least 8 hours long.",
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.error
            )
        }

        Spacer(Modifier.height(24.dp))
        HorizontalDivider()
        Spacer(Modifier.height(20.dp))

        Text(
            "Apps we'll track",
            fontSize = 16.sp, fontWeight = FontWeight.SemiBold
        )
        Spacer(Modifier.height(4.dp))
        Text(
            "We automatically include every supported social media, messaging, and AI " +
                "app you already have installed. You'll only see prompts when you open one " +
                "of these.",
            fontSize = 13.sp, color = MaterialTheme.colorScheme.secondary,
            lineHeight = 18.sp
        )
        Spacer(Modifier.height(12.dp))

        if (anyAppInstalled) {
            InstalledAppChips(installedNames)
        } else {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    "We couldn't find any supported apps on this device. The study needs " +
                        "at least one tracked app installed.",
                    fontSize = 13.sp, lineHeight = 18.sp,
                    color = MaterialTheme.colorScheme.onErrorContainer,
                    modifier = Modifier.padding(14.dp)
                )
            }
        }

        Spacer(Modifier.height(28.dp))
        Button(
            onClick = {
                onNext(startMin, endMin, installedPackages)
            },
            enabled = canContinue,
            modifier = Modifier.fillMaxWidth().height(52.dp)
        ) {
            Text("Continue", fontSize = 16.sp)
        }
        Spacer(Modifier.height(20.dp))
    }
}

@OptIn(androidx.compose.foundation.layout.ExperimentalLayoutApi::class)
@Composable
private fun InstalledAppChips(names: List<String>) {
    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        names.forEach { name ->
            Box(
                modifier = Modifier
                    .padding(vertical = 3.dp)
                    .background(MaterialTheme.colorScheme.primaryContainer, RoundedCornerShape(50))
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Text(
                    name,
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

/** Hour-precision time dropdown displaying 12-hour time with AM/PM. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TimeDropdown(
    label: String,
    minutesOfDay: Int,
    onChange: (Int) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val options = (0 until 24).map { it * 60 }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = it }
    ) {
        OutlinedTextField(
            value = SamplingManager.formatTime12h(minutesOfDay),
            onValueChange = {},
            label = { Text(label) },
            readOnly = true,
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor()
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            options.forEach { mins ->
                DropdownMenuItem(
                    text = { Text(SamplingManager.formatTime12h(mins)) },
                    onClick = {
                        onChange(mins)
                        expanded = false
                    }
                )
            }
        }
    }
}
