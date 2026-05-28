package com.gdc.todaytasks.ui

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gdc.todaytasks.data.RecurrenceTemplateEntity
import com.gdc.todaytasks.data.TaskDraft
import com.gdc.todaytasks.data.TaskEntity
import com.gdc.todaytasks.data.TaskRepository
import com.gdc.todaytasks.reminder.DayChangeScheduler
import com.gdc.todaytasks.reminder.NotificationController
import com.gdc.todaytasks.widget.TodayTasksWidgetProvider
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

@HiltViewModel
class TaskViewModel @Inject constructor(
    private val repository: TaskRepository,
    private val notificationController: NotificationController,
    private val scheduler: DayChangeScheduler,
    @ApplicationContext private val context: Context
) : ViewModel() {
    private val selectedDate = MutableStateFlow(LocalDate.now())
    private val deletedEvents = MutableSharedFlow<TaskEntity>(extraBufferCapacity = 1)
    val deleted = deletedEvents.asSharedFlow()

    val today: StateFlow<LocalDate> = selectedDate
    val todayTasks = selectedDate.flatMapLatest(repository::observeToday)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())
    val futureTasks = selectedDate.flatMapLatest(repository::observeFuture)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())
    val historyTasks = repository.observeHistory()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())
    val incompleteCount = selectedDate.flatMapLatest(repository::observeIncompleteCount)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 0)

    init {
        refreshDay()
        viewModelScope.launch {
            incompleteCount.collect(notificationController::updateTodayBadge)
        }
    }

    fun refreshDay() {
        val now = LocalDate.now()
        selectedDate.value = now
        viewModelScope.launch {
            repository.prepareDay(now)
            scheduler.scheduleNextRefresh()
            TodayTasksWidgetProvider.refreshAsync(context)
        }
    }

    fun save(draft: TaskDraft, existing: TaskEntity? = null) {
        viewModelScope.launch {
            if (existing == null) {
                repository.addTask(draft)
            } else {
                if (existing.recurrenceTemplateId == null && draft.recurrence != null) {
                    repository.convertToRepeating(existing.id, draft)
                } else {
                    repository.updateInstance(existing.id, draft.title, draft.isStarred, draft.date)
                }
                if (existing.recurrenceTemplateId != null && draft.recurrence != null) {
                    repository.updateTemplate(existing.recurrenceTemplateId, draft)
                }
            }
            repository.prepareDay(LocalDate.now())
            TodayTasksWidgetProvider.refreshAsync(context)
        }
    }

    suspend fun templateFor(task: TaskEntity): RecurrenceTemplateEntity? =
        task.recurrenceTemplateId?.let { repository.getTemplate(it) }

    fun toggleComplete(task: TaskEntity) {
        viewModelScope.launch {
            repository.setCompleted(task.id, !task.isCompleted, LocalDate.now())
            TodayTasksWidgetProvider.refreshAsync(context)
        }
    }

    fun toggleStar(task: TaskEntity) {
        viewModelScope.launch {
            repository.toggleStar(task.id)
            TodayTasksWidgetProvider.refreshAsync(context)
        }
    }

    fun delete(task: TaskEntity) {
        viewModelScope.launch {
            repository.deleteTask(task.id)?.let { deletedEvents.emit(it) }
            TodayTasksWidgetProvider.refreshAsync(context)
        }
    }

    fun undoDelete(task: TaskEntity) {
        viewModelScope.launch {
            repository.restoreTask(task)
            TodayTasksWidgetProvider.refreshAsync(context)
        }
    }

    fun stopRepeating(task: TaskEntity) {
        task.recurrenceTemplateId?.let { id ->
            viewModelScope.launch {
                repository.stopRepeating(id)
                TodayTasksWidgetProvider.refreshAsync(context)
            }
        }
    }

    fun moveTask(task: TaskEntity, direction: Int) {
        val samePriority = todayTasks.value.filter { it.isStarred == task.isStarred && !it.isCompleted }
        val index = samePriority.indexOfFirst { it.id == task.id }
        val targetIndex = index + direction
        if (index < 0 || targetIndex !in samePriority.indices) return
        val reordered = samePriority.toMutableList().apply {
            add(targetIndex, removeAt(index))
        }
        viewModelScope.launch {
            repository.reorder(reordered)
            TodayTasksWidgetProvider.refreshAsync(context)
        }
    }
}
