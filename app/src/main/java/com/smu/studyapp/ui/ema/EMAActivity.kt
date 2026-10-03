package com.smu.studyapp.ui.ema

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import com.smu.studyapp.service.EMAScheduler
import com.smu.studyapp.ui.theme.SMUStudyTheme

class EMAActivity : ComponentActivity() {
    private val vm: EMAViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val surveyType = intent.getStringExtra("survey_type") ?: "EMA_5PM"
        val triggerTimeMs = intent.getLongExtra("trigger_time_ms", System.currentTimeMillis())
        val expired = System.currentTimeMillis() - triggerTimeMs > EMAScheduler.SURVEY_EXPIRY_MS

        vm.init(surveyType)
        setContent {
            SMUStudyTheme {
                if (expired) {
                    ExpiredScreen(onDone = { finish() })
                } else {
                    EMAScreen(surveyType = surveyType, vm = vm, onDone = { finish() })
                }
            }
        }
    }
}
