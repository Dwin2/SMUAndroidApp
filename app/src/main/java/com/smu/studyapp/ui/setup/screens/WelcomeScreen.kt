package com.smu.studyapp.ui.setup.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun WelcomeScreen(onNext: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Welcome to the\nSMU Study",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(Modifier.height(24.dp))
        Text(
            text = "This 7-day study explores how awareness of your social media motivations relates to your well-being.\n\nYou'll receive brief prompts when you open social media apps, and short surveys twice a day.",
            fontSize = 16.sp,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(Modifier.height(16.dp))
        Text(
            text = "No content from your social media is ever read or recorded. Only your survey responses are collected.",
            fontSize = 14.sp,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.secondary
        )
        Spacer(Modifier.height(40.dp))
        Button(
            onClick = onNext,
            modifier = Modifier.fillMaxWidth().height(52.dp)
        ) {
            Text("Get Started", fontSize = 16.sp)
        }
    }
}
