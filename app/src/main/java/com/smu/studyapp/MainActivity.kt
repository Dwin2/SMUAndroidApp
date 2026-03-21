package com.smu.studyapp

import android.Manifest
import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
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

    private val notifPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { /* proceed regardless */ }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            notifPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }

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
                        participant?.baselineSurveyComplete == true -> Routes.TUTORIAL
                        participant != null -> Routes.BASELINE_SURVEY
                        else -> Routes.WELCOME
                    }

                    val navController = rememberNavController()

                    NavHost(navController = navController, startDestination = startDestination) {
                        composable(Routes.WELCOME) {
                            WelcomeScreen(onNext = { navController.navigate(Routes.DEMOGRAPHICS) })
                        }
                        composable(Routes.DEMOGRAPHICS) {
                            DemographicsScreen { name, age, gender, code, wStart, wEnd ->
                                setupVm.saveDemographics(name, age, gender, code, wStart, wEnd)
                                navController.navigate(Routes.BASELINE_SURVEY)
                            }
                        }
                        composable(Routes.BASELINE_SURVEY) {
                            BaselineSurveyScreen { responses ->
                                setupVm.saveBaselineSurvey(responses)
                                navController.navigate(Routes.TUTORIAL) {
                                    popUpTo(Routes.BASELINE_SURVEY) { inclusive = true }
                                }
                            }
                        }
                        composable(Routes.TUTORIAL) {
                            TutorialScreen(
                            onSaveSelectedApps = { setupVm.saveSelectedApps(it) },
                            onComplete = {
                                setupVm.completeSetup()
                                EMAScheduler.scheduleDaily(this@MainActivity)
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
                                // Restart the activity so ViewModels are fresh
                                // and routing re-evaluates from a clean DB
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
