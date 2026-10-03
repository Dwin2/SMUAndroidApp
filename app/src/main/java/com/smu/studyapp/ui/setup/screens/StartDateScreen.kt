package com.smu.studyapp.ui.setup.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.smu.studyapp.utils.SamplingManager
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Confirms the participant's scheduled Day 1 start. Shown right after the mock prompt
 * so the participant sees, in absolute date+time, when prompts will begin firing —
 * "Your study begins tomorrow, [date] at [window-start]."
 */
@Composable
fun StartDateScreen(
    windowStartMin: Int,
    onNext: () -> Unit
) {
    val startMs = SamplingManager.computeStudyStartDate(
        System.currentTimeMillis(), windowStartMin
    )
    val dateFmt = SimpleDateFormat("EEEE, MMMM d", Locale.getDefault())
    val dateLabel = dateFmt.format(Date(startMs))
    val timeLabel = SamplingManager.formatTime12h(windowStartMin)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(28.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.height(20.dp))
        Box(
            modifier = Modifier
                .size(84.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primary),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.Default.Schedule,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(46.dp)
            )
        }
        Spacer(Modifier.height(20.dp))
        Text(
            "That's it!",
            fontSize = 26.sp, fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(Modifier.height(12.dp))
        Text(
            "Your study begins tomorrow,",
            fontSize = 16.sp, textAlign = TextAlign.Center
        )
        Text(
            "$dateLabel at $timeLabel.",
            fontSize = 20.sp, fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.primary,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(16.dp))
        Text(
            "Today is set-up only — no prompts will fire and no daily check-ins will be " +
                "scheduled until your start time.",
            fontSize = 14.sp,
            lineHeight = 20.sp,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.secondary
        )

        Spacer(Modifier.height(28.dp))
        Button(
            onClick = onNext,
            modifier = Modifier.fillMaxWidth().height(52.dp)
        ) {
            Text("Continue", fontSize = 16.sp)
        }
        Spacer(Modifier.height(20.dp))
    }
}
