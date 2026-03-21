package com.smu.studyapp.ui.prompts

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import com.smu.studyapp.ui.theme.SMUStudyTheme

class PromptActivity : ComponentActivity() {

    companion object {
        const val EXTRA_SESSION_ID = "session_id"
        const val EXTRA_APP_PACKAGE = "app_package"
        const val EXTRA_PROMPT_TYPE = "prompt_type" // "T"=MRP, "C"=NP, "SATISFACTION"
    }

    private val vm: PromptViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val sessionId = intent.getStringExtra(EXTRA_SESSION_ID) ?: run { finish(); return }
        val appPackage = intent.getStringExtra(EXTRA_APP_PACKAGE) ?: ""
        val promptType = intent.getStringExtra(EXTRA_PROMPT_TYPE) ?: "C"

        vm.init(sessionId, appPackage, promptType)

        setContent {
            SMUStudyTheme {
                PromptScreen(
                    sessionId = sessionId,
                    appPackage = appPackage,
                    promptType = promptType,
                    vm = vm,
                    onDismiss = { finish() }
                )
            }
        }
    }
}
