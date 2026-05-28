package com.gdc.todaytasks

import android.Manifest
import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.os.Build
import android.os.Bundle
import android.content.Intent
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.gdc.todaytasks.ui.TaskViewModel
import com.gdc.todaytasks.ui.TodayTasksApp
import com.gdc.todaytasks.ui.theme.TodayTasksTheme
import com.gdc.todaytasks.widget.TodayTasksWidgetProvider
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private val viewModel: TaskViewModel by viewModels()
    private var editorRequest by mutableIntStateOf(0)
    private val notificationsPermission = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { viewModel.refreshDay() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (Build.VERSION.SDK_INT >= 33) {
            notificationsPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
        handleWidgetIntent(intent)
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.RESUMED) { viewModel.refreshDay() }
        }
        setContent {
            TodayTasksTheme {
                TodayTasksApp(viewModel, editorRequest, ::pinWidget)
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleWidgetIntent(intent)
    }

    private fun handleWidgetIntent(intent: Intent?) {
        if (intent?.getBooleanExtra(EXTRA_OPEN_EDITOR, false) == true) {
            editorRequest++
            intent.removeExtra(EXTRA_OPEN_EDITOR)
        }
    }

    private fun pinWidget() {
        val manager = AppWidgetManager.getInstance(this)
        if (manager.isRequestPinAppWidgetSupported) {
            manager.requestPinAppWidget(
                ComponentName(this, TodayTasksWidgetProvider::class.java),
                null,
                null
            )
        }
    }

    companion object {
        const val EXTRA_OPEN_EDITOR = "open_editor"
    }
}
