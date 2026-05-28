package com.gdc.todaytasks.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.view.View
import android.widget.RemoteViews
import com.gdc.todaytasks.MainActivity
import com.gdc.todaytasks.R
import com.gdc.todaytasks.data.AppDatabase
import com.gdc.todaytasks.data.TaskRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.LocalDate

class TodayTasksWidgetProvider : AppWidgetProvider() {
    override fun onUpdate(context: Context, manager: AppWidgetManager, ids: IntArray) {
        refreshAsync(context)
    }

    companion object {
        private val taskViews = intArrayOf(
            R.id.widget_task_1,
            R.id.widget_task_2,
            R.id.widget_task_3
        )

        fun refreshAsync(context: Context) {
            CoroutineScope(Dispatchers.IO).launch {
                refresh(context.applicationContext)
            }
        }

        suspend fun refresh(context: Context) {
            val database = AppDatabase.getInstance(context)
            val repository = TaskRepository(
                database,
                database.taskDao(),
                database.recurrenceDao(),
                database.historyGroupDao()
            )
            val today = LocalDate.now()
            repository.prepareDay(today)
            val tasks = database.taskDao().currentIncompleteTasks(today.toString(), taskViews.size)
            val count = database.taskDao().currentIncompleteCount(today.toString())
            val views = RemoteViews(context.packageName, R.layout.widget_today_tasks).apply {
                setTextViewText(R.id.widget_count, "$count 件")
                setViewVisibility(R.id.widget_empty, if (tasks.isEmpty()) View.VISIBLE else View.GONE)
                setViewVisibility(R.id.widget_spacer, if (tasks.isEmpty()) View.GONE else View.VISIBLE)
                taskViews.forEachIndexed { index, id ->
                    val task = tasks.getOrNull(index)
                    setTextViewText(id, task?.let { "${if (it.isStarred) "★ " else ""}${it.title}" } ?: "")
                    setViewVisibility(id, if (task == null) View.GONE else View.VISIBLE)
                }
                setOnClickPendingIntent(R.id.widget_root, activityIntent(context, false))
                setOnClickPendingIntent(R.id.widget_add, activityIntent(context, true))
            }
            val manager = AppWidgetManager.getInstance(context)
            val provider = ComponentName(context, TodayTasksWidgetProvider::class.java)
            manager.updateAppWidget(manager.getAppWidgetIds(provider), views)
        }

        private fun activityIntent(context: Context, openEditor: Boolean): PendingIntent {
            val intent = Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                putExtra(MainActivity.EXTRA_OPEN_EDITOR, openEditor)
            }
            return PendingIntent.getActivity(
                context,
                if (openEditor) 2 else 1,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
        }
    }
}
