package com.smu.studyapp.ui.setup.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun ExpectationsScreen(onNext: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp)
    ) {
        Text(
            "Here's what to expect",
            fontSize = 24.sp, fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(Modifier.height(6.dp))
        Text(
            "Here's how the study works 👇",
            fontSize = 15.sp,
            color = MaterialTheme.colorScheme.secondary
        )
        Spacer(Modifier.height(20.dp))
        Text(
            "You'll get 2 types of short pop-ups during the study:",
            fontSize = 15.sp, fontWeight = FontWeight.Medium
        )
        Spacer(Modifier.height(16.dp))

        ExpectationCard(
            badge = "①",
            title = "When you open an app",
            body = "A quick question will appear with a question. Just type a short response and you're done. Takes about 30 seconds.\n\nWhen you close the app, we'll ask one more question: \"How was that session?\""
        )
        Spacer(Modifier.height(12.dp))
        ExpectationCard(
            badge = "②",
            title = "Twice a day — at 5 PM and 9 PM",
            body = "A 1-minute check-in about how you're feeling and how your app use went."
        )

        Spacer(Modifier.height(24.dp))
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                "We respect your time and attention. You'll see no more than 15 prompts per day. " +
                    "Once you hit that, the pop-ups pause until tomorrow.",
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.primary,
                lineHeight = 20.sp,
                modifier = Modifier.padding(16.dp)
            )
        }

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

@Composable
private fun ExpectationCard(badge: String, title: String, body: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.Top) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary),
                contentAlignment = Alignment.Center
            ) {
                Text(badge, color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
            }
            Spacer(Modifier.width(12.dp))
            Column {
                Text(title, fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
                Spacer(Modifier.height(6.dp))
                Text(
                    body, fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.secondary,
                    lineHeight = 19.sp
                )
            }
        }
    }
}
