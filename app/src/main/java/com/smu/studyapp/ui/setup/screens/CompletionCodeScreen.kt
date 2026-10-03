package com.smu.studyapp.ui.setup.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

const val COMPLETION_CODE = "HAPPY999"

@Composable
fun CompletionCodeScreen(onGoDashboard: () -> Unit) {
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(28.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.height(24.dp))
        Box(
            modifier = Modifier
                .size(84.dp)
                .clip(CircleShape)
                .background(Color(0xFF4CAF50)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.Default.Check, contentDescription = null,
                tint = Color.White, modifier = Modifier.size(48.dp)
            )
        }
        Spacer(Modifier.height(20.dp))
        Text(
            "All set!",
            fontSize = 28.sp, fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(Modifier.height(8.dp))
        Text(
            "You're all set! 🎉",
            fontSize = 16.sp, fontWeight = FontWeight.Medium
        )
        Spacer(Modifier.height(16.dp))
        Text(
            "Your app is ready to go. Starting tomorrow, you'll begin receiving study prompts automatically.",
            fontSize = 15.sp, lineHeight = 22.sp, textAlign = TextAlign.Center
        )

        Spacer(Modifier.height(24.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
            elevation = CardDefaults.cardElevation(2.dp)
        ) {
            Column(
                Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    "One last step:",
                    fontSize = 13.sp, fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(Modifier.height(6.dp))
                Text(
                    "Head back to your Prolific survey and enter the following completion code to confirm your app is set up:",
                    fontSize = 14.sp, lineHeight = 20.sp, textAlign = TextAlign.Center
                )
                Spacer(Modifier.height(16.dp))

                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.surface)
                        .padding(horizontal = 20.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        COMPLETION_CODE,
                        fontSize = 26.sp, fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(end = 12.dp)
                    )
                    IconButton(onClick = {
                        val cm = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        cm.setPrimaryClip(ClipData.newPlainText("completion code", COMPLETION_CODE))
                        Toast.makeText(context, "Copied", Toast.LENGTH_SHORT).show()
                    }) {
                        Icon(Icons.Default.ContentCopy, contentDescription = "Copy")
                    }
                }
            }
        }

        Spacer(Modifier.height(20.dp))
        Card(
            colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF8E1)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                "Keep this app installed on your phone for the full 7 days. You can uninstall it after you complete the endline survey.",
                fontSize = 13.sp, lineHeight = 18.sp,
                color = Color(0xFF6D4C00),
                modifier = Modifier.padding(16.dp)
            )
        }

        Spacer(Modifier.height(28.dp))
        Button(
            onClick = onGoDashboard,
            modifier = Modifier.fillMaxWidth().height(52.dp)
        ) {
            Text("Go to Dashboard", fontSize = 16.sp)
        }
        Spacer(Modifier.height(16.dp))
    }
}
