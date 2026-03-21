package com.smu.studyapp.ui.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.foundation.combinedClickable
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.gson.Gson
import com.smu.studyapp.data.entities.AppSession
import com.smu.studyapp.data.entities.SurveyResponse
import com.smu.studyapp.utils.SamplingManager
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class, androidx.compose.foundation.ExperimentalFoundationApi::class)
@Composable
fun DashboardScreen(vm: DashboardViewModel = viewModel(), onReset: () -> Unit = {}) {
    val state by vm.uiState.collectAsState()
    var showResetDialog by remember { mutableStateOf(false) }

    if (showResetDialog) {
        AlertDialog(
            onDismissRequest = { showResetDialog = false },
            title = { Text("Reset Study Data?") },
            text = { Text("This will delete all study data and return to the setup screen. Use this for testing only.") },
            confirmButton = {
                TextButton(onClick = {
                    showResetDialog = false
                    vm.resetStudy(onReset)
                }) { Text("Reset", color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = {
                TextButton(onClick = { showResetDialog = false }) { Text("Cancel") }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "SMU Study Dashboard",
                        modifier = Modifier.combinedClickable(
                            onClick = {},
                            onLongClick = { showResetDialog = true }
                        )
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = Color.White
                )
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item { Spacer(Modifier.height(8.dp)) }

            // Study status header
            item {
                state.participant?.let { p ->
                    StudyStatusCard(
                        name = p.name,
                        studyDay = state.studyDay,
                        group = p.studyGroup,
                        windowStart = p.samplingWindowStart,
                        windowEnd = p.samplingWindowEnd
                    )
                }
            }

            // Today's stats
            item {
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    StatCard(
                        label = "Prompts Today",
                        value = "${state.todayPromptCount} / ${SamplingManager.MAX_PROMPTS_PER_DAY}",
                        modifier = Modifier.weight(1f)
                    )
                    StatCard(
                        label = "Sessions Today",
                        value = "${state.todaySessions.size}",
                        modifier = Modifier.weight(1f)
                    )
                    StatCard(
                        label = "Total Sessions",
                        value = "${state.totalSessions}",
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // Survey reminders note
            item {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                ) {
                    Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Notifications, contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary)
                        Spacer(Modifier.width(12.dp))
                        Column {
                            Text("Daily Surveys", fontWeight = FontWeight.SemiBold)
                            Text("5:00 PM and 9:00 PM reminders are active.",
                                fontSize = 13.sp, color = MaterialTheme.colorScheme.secondary)
                        }
                    }
                }
            }

            // Tracked apps
            if (state.trackedAppNames.isNotEmpty()) {
                item {
                    Text("Tracked Apps", fontWeight = FontWeight.SemiBold, fontSize = 16.sp,
                        modifier = Modifier.padding(top = 4.dp))
                }
                item {
                    FlowRow(state.trackedAppNames)
                }
            }

            // Prompt history grouped by session
            item {
                Text("Prompt History", fontWeight = FontWeight.SemiBold, fontSize = 16.sp,
                    modifier = Modifier.padding(top = 4.dp))
            }
            if (state.promptHistory.isEmpty()) {
                item {
                    Text("No prompts yet. Open a tracked app to get started.",
                        fontSize = 13.sp, color = MaterialTheme.colorScheme.secondary)
                }
            } else {
                // Group by sessionId so open+close appear together
                val grouped = state.promptHistory
                    .groupBy { it.sessionId.ifBlank { it.id.toString() } }
                    .entries.toList()
                items(grouped) { (_, responses) ->
                    SessionPromptGroup(responses)
                }
            }

            item { Spacer(Modifier.height(16.dp)) }
        }
    }
}

@Composable
private fun StudyStatusCard(
    name: String, studyDay: Int, group: String, windowStart: Int, windowEnd: Int
) {
    val dayLabel = when {
        studyDay == 0 -> "Day 0 – Baseline"
        studyDay in 1..7 -> "Day $studyDay of 7 – Study Active"
        studyDay == 8 -> "Day 8 – Endline"
        studyDay >= 30 -> "Day 30 – Follow-up"
        else -> "Day $studyDay"
    }
    val groupLabel = if (group == "T") "Treatment" else "Control"

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(3.dp)
    ) {
        Column(Modifier.padding(16.dp)) {
            Text("Hello, $name!", fontSize = 20.sp, fontWeight = FontWeight.Bold)
            Text(dayLabel, fontSize = 15.sp, color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Chip("Group: $groupLabel")
                Chip("Window: ${windowStart}:00–${windowEnd}:00")
            }
        }
    }
}

@Composable
private fun Chip(text: String) {
    Box(
        modifier = Modifier
            .background(
                MaterialTheme.colorScheme.primaryContainer,
                shape = RoundedCornerShape(50)
            )
            .padding(horizontal = 10.dp, vertical = 4.dp)
    ) {
        Text(text, fontSize = 12.sp, color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.Medium)
    }
}

@Composable
private fun StatCard(label: String, value: String, modifier: Modifier = Modifier) {
    Card(modifier = modifier, elevation = CardDefaults.cardElevation(2.dp)) {
        Column(
            Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(value, fontSize = 22.sp, fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary)
            Text(label, fontSize = 11.sp, color = MaterialTheme.colorScheme.secondary)
        }
    }
}

@Composable
private fun SessionRow(session: AppSession) {
    val fmt = SimpleDateFormat("h:mm a", Locale.getDefault())
    val openStr = fmt.format(Date(session.openTime))
    val closeStr = if (session.closeTime > 0) fmt.format(Date(session.closeTime)) else "—"
    val duration = if (session.closeTime > 0) {
        val mins = (session.closeTime - session.openTime) / 60000
        "${mins}m"
    } else "active"

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (session.promptShown)
                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
            else MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(Modifier.weight(1f)) {
                Text(session.appName, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                Text("$openStr – $closeStr  ($duration)",
                    fontSize = 12.sp, color = MaterialTheme.colorScheme.secondary)
            }
            if (session.promptShown) {
                val tag = if (session.promptType == "T") "MRP" else "NP"
                Box(
                    modifier = Modifier
                        .background(MaterialTheme.colorScheme.primary, RoundedCornerShape(4.dp))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(tag, fontSize = 11.sp, color = Color.White, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@OptIn(androidx.compose.foundation.layout.ExperimentalLayoutApi::class)
@Composable
private fun FlowRow(appNames: List<String>) {
    androidx.compose.foundation.layout.FlowRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        appNames.forEach { name ->
            Box(
                modifier = Modifier
                    .padding(vertical = 3.dp)
                    .background(MaterialTheme.colorScheme.primaryContainer, RoundedCornerShape(50))
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Text(name, fontSize = 13.sp, color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Medium)
            }
        }
    }
}

@Composable
private fun SessionPromptGroup(responses: List<SurveyResponse>) {
    val fmt = SimpleDateFormat("MMM d, h:mm a", Locale.getDefault())
    val appName = SamplingManager.getAppName(responses.first().appPackage).ifBlank { "—" }
    val earliest = responses.minOf { it.timestamp }

    Card(modifier = Modifier.fillMaxWidth(), elevation = CardDefaults.cardElevation(2.dp)) {
        Column(Modifier.padding(12.dp)) {
            // Session header
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(appName, fontWeight = FontWeight.Bold, fontSize = 14.sp,
                    modifier = Modifier.weight(1f))
                Text(fmt.format(Date(earliest)),
                    fontSize = 11.sp, color = MaterialTheme.colorScheme.secondary)
            }
            Spacer(Modifier.height(8.dp))
            // Each prompt in the session
            responses.sortedBy { it.timestamp }.forEach { response ->
                PromptHistoryRow(response)
                Spacer(Modifier.height(6.dp))
            }
        }
    }
}

@Composable
private fun PromptHistoryRow(response: SurveyResponse) {
    val fmt = SimpleDateFormat("h:mm a", Locale.getDefault())
    val timeStr = fmt.format(Date(response.timestamp))
    val typeLabel = when (response.surveyType) {
        "MRP" -> "Opening prompt"
        "NP" -> "Opening prompt"
        "SATISFACTION" -> "Closing survey"
        else -> response.surveyType
    }
    val responseText = try {
        val map = Gson().fromJson(response.responseJson, Map::class.java)
        when (response.surveyType) {
            "MRP" -> map["motivation"]?.toString() ?: ""
            "NP" -> map["answer"]?.toString() ?: ""
            "SATISFACTION" -> map["satisfaction"]?.toString() ?: ""
            else -> ""
        }
    } catch (e: Exception) { "" }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(6.dp))
            .padding(horizontal = 10.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .background(MaterialTheme.colorScheme.primaryContainer, RoundedCornerShape(4.dp))
                .padding(horizontal = 6.dp, vertical = 2.dp)
        ) {
            Text(typeLabel, fontSize = 11.sp, color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.SemiBold)
        }
        Spacer(Modifier.width(8.dp))
        Text(
            if (responseText.isNotBlank()) "\"$responseText\"" else "Skipped",
            fontSize = 13.sp,
            color = if (responseText.isNotBlank()) MaterialTheme.colorScheme.onSurface
                    else MaterialTheme.colorScheme.secondary,
            modifier = Modifier.weight(1f)
        )
        Spacer(Modifier.width(8.dp))
        Text(timeStr, fontSize = 11.sp, color = MaterialTheme.colorScheme.secondary)
    }
}
