package com.smu.studyapp.ui.setup.screens

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
import com.smu.studyapp.ui.setup.SetupViewModel.EnrollState
import com.smu.studyapp.utils.SamplingManager

/**
 * Collects the Prolific ID, reads it back for confirmation (a typo would claim an allocation
 * slot under a non-existent ID), then enrolls it with the backend via [onConfirmed].
 */
@Composable
fun ProlificIdScreen(
    enrollState: EnrollState,
    onConfirmed: (prolificId: String) -> Unit,
    onDismissError: () -> Unit
) {
    var input by remember { mutableStateOf("") }
    var attemptedSubmit by remember { mutableStateOf(false) }
    var confirming by remember { mutableStateOf(false) }
    val loading = enrollState == EnrollState.Loading

    val isValid = SamplingManager.isValidProlificId(input)
    val showError = attemptedSubmit && !isValid

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(28.dp)
    ) {
        Spacer(Modifier.height(40.dp))
        Text(
            "Welcome to the\nSMU Study App!",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(28.dp))
        Text(
            "Before we get started, please enter your Prolific ID below. This is how we link your responses to your compensation.",
            fontSize = 15.sp,
            lineHeight = 22.sp
        )
        Spacer(Modifier.height(28.dp))
        Text("My Prolific ID is:", fontSize = 14.sp, fontWeight = FontWeight.Medium)
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(
            value = input,
            onValueChange = {
                input = it.trim().lowercase()
                attemptedSubmit = false
            },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            enabled = !loading,
            isError = showError,
            placeholder = { Text("e.g. 5a9d64f5f6dfdd0001eaa73d", fontSize = 14.sp) },
            supportingText = {
                if (showError) {
                    Text(
                        "Please enter a valid 24-character Prolific ID",
                        color = MaterialTheme.colorScheme.error
                    )
                } else {
                    Text("${input.length}/24 characters", fontSize = 12.sp)
                }
            }
        )
        Spacer(Modifier.weight(1f))
        Spacer(Modifier.height(24.dp))
        Button(
            onClick = {
                attemptedSubmit = true
                if (isValid) confirming = true
            },
            enabled = !loading,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
        ) {
            if (loading) {
                CircularProgressIndicator(Modifier.size(22.dp), strokeWidth = 2.dp)
            } else {
                Text("Continue", fontSize = 16.sp)
            }
        }
        Spacer(Modifier.height(20.dp))
    }

    if (confirming) {
        AlertDialog(
            onDismissRequest = { confirming = false },
            title = { Text("Is this your Prolific ID?") },
            text = {
                Text(
                    input,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold,
                    fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    confirming = false
                    onConfirmed(input)
                }) { Text("Yes, continue") }
            },
            dismissButton = {
                TextButton(onClick = { confirming = false }) { Text("Edit") }
            }
        )
    }

    if (enrollState is EnrollState.Error) {
        AlertDialog(
            onDismissRequest = onDismissError,
            title = { Text("Couldn't connect") },
            text = {
                Text("We couldn't register your Prolific ID. Please check your internet connection and try again.")
            },
            confirmButton = {
                TextButton(onClick = {
                    onDismissError()
                    onConfirmed(input)
                }) { Text("Try again") }
            },
            dismissButton = { TextButton(onClick = onDismissError) { Text("Close") } }
        )
    }
}
