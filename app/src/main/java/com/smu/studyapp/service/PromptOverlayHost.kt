package com.smu.studyapp.service

import android.content.Context
import android.graphics.Color
import android.graphics.PixelFormat
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.util.Log
import android.view.ContextThemeWrapper
import android.view.Gravity
import android.view.KeyEvent
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.FrameLayout
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.setViewTreeLifecycleOwner
import androidx.lifecycle.setViewTreeViewModelStoreOwner
import androidx.savedstate.SavedStateRegistry
import androidx.savedstate.SavedStateRegistryController
import androidx.savedstate.SavedStateRegistryOwner
import androidx.savedstate.setViewTreeSavedStateRegistryOwner
import com.smu.studyapp.MyApplication
import com.smu.studyapp.R
import com.smu.studyapp.ui.prompts.PromptScreen
import com.smu.studyapp.ui.prompts.PromptViewModel
import com.smu.studyapp.ui.theme.SMUStudyTheme

/**
 * Renders MRP / NP / SATISFACTION prompts as a system overlay (TYPE_APPLICATION_OVERLAY)
 * instead of starting an Activity. This avoids Android 13+ background-activity-launch
 * restrictions and OEM activity-start blocks; the overlay is added directly via
 * WindowManager from the AccessibilityService process.
 *
 * Only one overlay is shown at a time. Showing while another is visible replaces it.
 */
class PromptOverlayHost(private val service: AppMonitorService) {

    private val TAG = "PromptOverlayHost"
    private val app: MyApplication = service.application as MyApplication
    private val mainHandler = Handler(Looper.getMainLooper())
    private val windowManager: WindowManager =
        service.getSystemService(Context.WINDOW_SERVICE) as WindowManager

    private var current: Showing? = null

    private class Showing(
        val rootView: OverlayRoot,
        val owner: OverlayLifecycleOwner,
        val sessionId: String,
        val packageName: String,
        val promptType: String,
        val mode: String,
        val nudgeTrigger: String,
        var submitted: Boolean = false
    )

    fun show(
        sessionId: String,
        packageName: String,
        promptType: String,
        mode: String = com.smu.studyapp.utils.NudgeManager.MODE_STANDARD,
        nudgeTrigger: String = com.smu.studyapp.utils.NudgeManager.TRIGGER_NONE,
        studyDay: Int = 1,
        onSkipped: (sessionId: String, packageName: String, promptType: String, mode: String, nudgeTrigger: String) -> Unit
    ) {
        mainHandler.post {
            showInternal(sessionId, packageName, promptType, mode, nudgeTrigger, studyDay, onSkipped)
        }
    }

    fun hideAll() {
        mainHandler.post { removeInternal(notifySkip = false, onSkipped = null) }
    }

    private fun showInternal(
        sessionId: String,
        packageName: String,
        promptType: String,
        mode: String,
        nudgeTrigger: String,
        studyDay: Int,
        onSkipped: (sessionId: String, packageName: String, promptType: String, mode: String, nudgeTrigger: String) -> Unit
    ) {
        if (!Settings.canDrawOverlays(app)) {
            Log.w(TAG, "canDrawOverlays=false; skipping overlay (sessionId=$sessionId)")
            return
        }

        // One overlay at a time. Removing the previous one without firing its skip callback
        // — we're explicitly replacing it.
        removeInternal(notifySkip = false, onSkipped = null)

        val owner = OverlayLifecycleOwner().also { it.performRestore() }
        val themedCtx = ContextThemeWrapper(app, R.style.Theme_SMUStudyApp)
        val root = OverlayRoot(themedCtx).apply {
            // Scrim behind the card; matches the prior translucent activity look.
            setBackgroundColor(0x99000000.toInt())
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
        }

        val vm = PromptViewModel(app).apply {
            init(sessionId, packageName, promptType, mode, nudgeTrigger)
        }

        val composeView = ComposeView(themedCtx).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnDetachedFromWindow)
            setContent {
                SMUStudyTheme {
                    PromptScreen(
                        sessionId = sessionId,
                        appPackage = packageName,
                        promptType = promptType,
                        vm = vm,
                        onSubmitDone = {
                            current?.submitted = true
                            removeInternal(notifySkip = false, onSkipped = null)
                        },
                        onSkip = {
                            removeInternal(notifySkip = true, onSkipped = onSkipped)
                        },
                        mode = mode,
                        studyDay = studyDay
                    )
                }
            }
        }
        root.addView(
            composeView,
            FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT
            )
        )

        // Compose looks up these owners on the view tree; without them ComposeView crashes.
        root.setViewTreeLifecycleOwner(owner)
        root.setViewTreeSavedStateRegistryOwner(owner)
        root.setViewTreeViewModelStoreOwner(owner)

        // SATISFACTION (app-close) cannot be skipped — back key is a no-op.
        // Open prompts (T/C) still allow BACK as skip.
        root.onBack = if (promptType == "SATISFACTION") {
            { /* swallow back press */ }
        } else {
            { removeInternal(notifySkip = true, onSkipped = onSkipped) }
        }

        val lp = WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            // Focusable (no FLAG_NOT_FOCUSABLE) so the EditText keyboard works and the
            // BACK key is delivered to the overlay's dispatchKeyEvent.
            WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.CENTER
        }

        try {
            windowManager.addView(root, lp)
        } catch (t: Throwable) {
            Log.e(TAG, "addView failed", t)
            owner.destroy()
            return
        }

        owner.moveTo(Lifecycle.State.RESUMED)
        current = Showing(root, owner, sessionId, packageName, promptType, mode, nudgeTrigger)
        Log.i(TAG, "overlay shown sessionId=$sessionId pkg=$packageName type=$promptType")
    }

    private fun removeInternal(
        notifySkip: Boolean,
        onSkipped: ((sessionId: String, packageName: String, promptType: String, mode: String, nudgeTrigger: String) -> Unit)?
    ) {
        val showing = current ?: return
        current = null

        try {
            windowManager.removeViewImmediate(showing.rootView)
        } catch (t: Throwable) {
            Log.w(TAG, "removeView failed (already removed?)", t)
        }
        showing.owner.destroy()

        if (notifySkip && !showing.submitted && onSkipped != null) {
            onSkipped(showing.sessionId, showing.packageName, showing.promptType, showing.mode, showing.nudgeTrigger)
        }
    }

    /** FrameLayout that routes BACK key to a callback (so the overlay behaves like a dialog). */
    private class OverlayRoot(context: Context) : FrameLayout(context) {
        var onBack: (() -> Unit)? = null
        override fun dispatchKeyEvent(event: KeyEvent): Boolean {
            if (event.keyCode == KeyEvent.KEYCODE_BACK && event.action == KeyEvent.ACTION_UP) {
                onBack?.invoke()
                return true
            }
            return super.dispatchKeyEvent(event)
        }

        init {
            // Background color set by caller; this just ensures we draw under the card.
            setBackgroundColor(Color.TRANSPARENT)
        }
    }

    /**
     * Minimum surface needed for a ComposeView to attach outside an Activity.
     * Compose requires LifecycleOwner + SavedStateRegistryOwner on the view tree.
     * ViewModelStoreOwner is provided too in case any callee uses viewModel().
     */
    private class OverlayLifecycleOwner :
        LifecycleOwner, SavedStateRegistryOwner, ViewModelStoreOwner {

        private val lifecycleRegistry = LifecycleRegistry(this)
        private val savedStateController = SavedStateRegistryController.create(this)
        private val store = ViewModelStore()

        override val lifecycle: Lifecycle get() = lifecycleRegistry
        override val savedStateRegistry: SavedStateRegistry
            get() = savedStateController.savedStateRegistry
        override val viewModelStore: ViewModelStore get() = store

        fun performRestore() {
            savedStateController.performAttach()
            savedStateController.performRestore(null)
            lifecycleRegistry.currentState = Lifecycle.State.CREATED
        }

        fun moveTo(state: Lifecycle.State) {
            lifecycleRegistry.currentState = state
        }

        fun destroy() {
            lifecycleRegistry.currentState = Lifecycle.State.DESTROYED
            store.clear()
        }
    }
}
