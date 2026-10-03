package com.smu.studyapp

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.smu.studyapp.navigation.Routes
import com.smu.studyapp.service.EMAScheduler
import com.smu.studyapp.service.MonitorForegroundService
import com.smu.studyapp.ui.dashboard.DashboardScreen
import com.smu.studyapp.ui.setup.SetupViewModel
import com.smu.studyapp.ui.setup.screens.*
import com.smu.studyapp.ui.theme.SMUStudyTheme

class MainActivity : ComponentActivity() {

    private val setupVm: SetupViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            SMUStudyTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val participant by setupVm.participant.collectAsState()
                    val loaded by setupVm.loaded.collectAsState()

                    if (!loaded) return@Surface

                    val startDestination = when {
                        participant?.setupComplete == true -> Routes.DASHBOARD
                        participant?.studyGroup == "T" || participant?.studyGroup == "C" ->
                            Routes.NOTIFICATIONS
                        else -> Routes.PROLIFIC_ID
                    }

                    val navController = rememberNavController()

                    NavHost(navController = navController, startDestination = startDestination) {
                        composable(Routes.PROLIFIC_ID) {
                            val enrollState by setupVm.enrollState.collectAsState()
                            LaunchedEffect(enrollState) {
                                if (enrollState == SetupViewModel.EnrollState.Done) {
                                    setupVm.resetEnrollState()
                                    navController.navigate(Routes.NOTIFICATIONS) {
                                        popUpTo(Routes.PROLIFIC_ID) { inclusive = true }
                                    }
                                }
                            }
                            ProlificIdScreen(
                                enrollState = enrollState,
                                onConfirmed = { setupVm.enroll(it) },
                                onDismissError = { setupVm.resetEnrollState() }
                            )
                        }
                        composable(Routes.NOTIFICATIONS) {
                            NotificationsScreen(onNext = {
                                navController.navigate(Routes.SAMPLING_WINDOW)
                            })
                        }
                        composable(Routes.SAMPLING_WINDOW) {
                            SamplingWindowScreen(
                                onNext = { startMin, endMin, selectedApps ->
                                    setupVm.saveSamplingWindow(startMin, endMin, selectedApps)
                                    navController.navigate(Routes.PERMISSIONS)
                                }
                            )
                        }
                        composable(Routes.PERMISSIONS) {
                            PermissionsScreen(onNext = {
                                navController.navigate(Routes.EXPECTATIONS)
                            })
                        }
                        composable(Routes.EXPECTATIONS) {
                            ExpectationsScreen(onNext = {
                                navController.navigate(Routes.MOCK_PROMPT)
                            })
                        }
                        composable(Routes.MOCK_PROMPT) {
                            MockPromptScreen(
                                studyGroup = participant?.studyGroup ?: "T",
                                onNext = { navController.navigate(Routes.START_DATE) }
                            )
                        }
                        composable(Routes.START_DATE) {
                            StartDateScreen(
                                windowStartMin = participant?.samplingWindowStartMin ?: (8 * 60),
                                onNext = { navController.navigate(Routes.COMPLETION_CODE) }
                            )
                        }
                        composable(Routes.COMPLETION_CODE) {
                            CompletionCodeScreen(onGoDashboard = {
                                setupVm.completeSetup()
                                // EMAs are deferred so install day stays setup-only — they
                                // begin firing on Day 1 (the participant's chosen window-
                                // start tomorrow).
                                val startMs = participant?.let {
                                    com.smu.studyapp.utils.SamplingManager.computeStudyStartDate(
                                        System.currentTimeMillis(), it.samplingWindowStartMin
                                    )
                                } ?: 0L
                                EMAScheduler.scheduleDaily(this@MainActivity, startMs)
                                startForegroundService(
                                    Intent(this@MainActivity, MonitorForegroundService::class.java)
                                )
                                navController.navigate(Routes.DASHBOARD) {
                                    popUpTo(0) { inclusive = true }
                                }
                            })
                        }
                        composable(Routes.DASHBOARD) {
                            DashboardScreen(onReset = {
                                finish()
                                startActivity(intent)
                            })
                        }
                    }
                }
            }
        }
    }
}
