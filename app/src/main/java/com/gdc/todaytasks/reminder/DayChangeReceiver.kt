package com.gdc.todaytasks.reminder

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.gdc.todaytasks.data.AppDatabase
import com.gdc.todaytasks.data.TaskRepository
import com.gdc.todaytasks.widget.TodayTasksWidgetProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.time.LocalDate

class DayChangeReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        if (intent?.action !in SUPPORTED_ACTIONS) return
        val result = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val database = AppDatabase.getInstance(context)
                val repository = TaskRepository(
                    database,
                    database.taskDao(),
                    database.recurrenceDao(),
                    database.historyGroupDao()
                )
                val today = LocalDate.now()
                repository.prepareDay(today)
                val count = repository.observeIncompleteCount(today).first()
                NotificationController(context.applicationContext).updateTodayBadge(count)
                TodayTasksWidgetProvider.refresh(context.applicationContext)
                DayChangeScheduler(context.applicationContext).scheduleNextRefresh()
            } finally {
                result.finish()
            }
        }
    }

    companion object {
        const val ACTION_MIDNIGHT_REFRESH = "com.gdc.todaytasks.action.MIDNIGHT_REFRESH"
        private val SUPPORTED_ACTIONS = setOf(
            ACTION_MIDNIGHT_REFRESH,
            Intent.ACTION_BOOT_COMPLETED,
            Intent.ACTION_TIME_CHANGED,
            Intent.ACTION_TIMEZONE_CHANGED
        )
    }
}
