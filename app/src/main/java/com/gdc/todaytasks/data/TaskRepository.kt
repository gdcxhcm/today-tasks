package com.gdc.todaytasks.data

import androidx.room.withTransaction
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TaskRepository @Inject constructor(
    private val database: AppDatabase,
    private val taskDao: TaskDao,
    private val recurrenceDao: RecurrenceDao,
    private val historyGroupDao: HistoryGroupDao
) {
    fun observeToday(date: LocalDate): Flow<List<TaskEntity>> = taskDao.observeToday(date.toString())
    fun observeFuture(date: LocalDate): Flow<List<TaskEntity>> = taskDao.observeFuture(date.toString())
    fun observeHistory(): Flow<List<TaskEntity>> = taskDao.observeHistory()
    fun observeHistoryGroups(): Flow<List<HistoryGroupEntity>> = historyGroupDao.observeGroups()
    fun observeIncompleteCount(date: LocalDate): Flow<Int> = taskDao.observeIncompleteCount(date.toString())

    suspend fun getTemplate(templateId: Long): RecurrenceTemplateEntity? = recurrenceDao.getById(templateId)

    suspend fun prepareDay(today: LocalDate) = database.withTransaction {
        recurrenceDao.activeTemplates()
            .filter { RecurrenceRules.isFixed(RecurrenceKind.valueOf(it.kind)) }
            .forEach { template -> generateFixedOccurrencesThrough(template, today) }
        taskDao.carryIncompleteInto(today.toString(), System.currentTimeMillis())
    }

    suspend fun addTask(draft: TaskDraft) = database.withTransaction {
        val title = draft.title.trim()
        if (title.isBlank()) return@withTransaction
        val recurrence = draft.recurrence
        if (recurrence == null) {
            insertOccurrence(title, draft.date, draft.isStarred)
            return@withTransaction
        }
        val templateId = recurrenceDao.insert(
            RecurrenceTemplateEntity(
                title = title,
                isStarred = draft.isStarred,
                kind = recurrence.kind.name,
                interval = recurrence.interval.coerceAtLeast(1),
                weekdays = RecurrenceRules.weekdaysToText(
                    recurrence.weekdays.ifEmpty { setOf(draft.date.dayOfWeek) }
                ),
                startDate = draft.date.toString()
            )
        )
        val firstDate = if (RecurrenceRules.isFixed(recurrence.kind)) {
            RecurrenceRules.firstFixedOccurrence(draft)
        } else {
            draft.date
        }
        insertOccurrence(
            title = title,
            date = firstDate,
            isStarred = draft.isStarred,
            templateId = templateId,
            occurrenceKey = "$templateId:${firstDate}"
        )
        if (RecurrenceRules.isFixed(recurrence.kind)) {
            recurrenceDao.update(
                requireNotNull(recurrenceDao.getById(templateId)).copy(
                    lastGeneratedDate = firstDate.toString()
                )
            )
        }
    }

    suspend fun updateInstance(taskId: Long, title: String, starred: Boolean, date: LocalDate) {
        val task = taskDao.getTask(taskId) ?: return
        taskDao.update(
            task.copy(
                title = title.trim().ifBlank { task.title },
                isStarred = starred,
                scheduledDate = date.toString(),
                updatedAt = System.currentTimeMillis()
            )
        )
    }

    suspend fun updateTemplate(templateId: Long, draft: TaskDraft) {
        val template = recurrenceDao.getById(templateId) ?: return
        val recurrence = draft.recurrence ?: return
        recurrenceDao.update(
            template.copy(
                title = draft.title.trim().ifBlank { template.title },
                isStarred = draft.isStarred,
                kind = recurrence.kind.name,
                interval = recurrence.interval.coerceAtLeast(1),
                weekdays = RecurrenceRules.weekdaysToText(
                    recurrence.weekdays.ifEmpty { setOf(draft.date.dayOfWeek) }
                ),
                startDate = draft.date.toString(),
                updatedAt = System.currentTimeMillis()
            )
        )
    }

    suspend fun convertToRepeating(taskId: Long, draft: TaskDraft) = database.withTransaction {
        val task = taskDao.getTask(taskId) ?: return@withTransaction
        val recurrence = draft.recurrence ?: return@withTransaction
        val firstDate = if (RecurrenceRules.isFixed(recurrence.kind)) {
            RecurrenceRules.firstFixedOccurrence(draft)
        } else {
            draft.date
        }
        val templateId = recurrenceDao.insert(
            RecurrenceTemplateEntity(
                title = draft.title.trim().ifBlank { task.title },
                isStarred = draft.isStarred,
                kind = recurrence.kind.name,
                interval = recurrence.interval.coerceAtLeast(1),
                weekdays = RecurrenceRules.weekdaysToText(
                    recurrence.weekdays.ifEmpty { setOf(draft.date.dayOfWeek) }
                ),
                startDate = draft.date.toString(),
                lastGeneratedDate = if (RecurrenceRules.isFixed(recurrence.kind)) firstDate.toString() else null
            )
        )
        taskDao.update(
            task.copy(
                title = draft.title.trim().ifBlank { task.title },
                isStarred = draft.isStarred,
                scheduledDate = firstDate.toString(),
                originDate = firstDate.toString(),
                isCarried = false,
                recurrenceTemplateId = templateId,
                occurrenceKey = "$templateId:$firstDate",
                updatedAt = System.currentTimeMillis()
            )
        )
    }

    suspend fun stopRepeating(templateId: Long) {
        val template = recurrenceDao.getById(templateId) ?: return
        recurrenceDao.update(template.copy(isActive = false, updatedAt = System.currentTimeMillis()))
    }

    suspend fun setCompleted(taskId: Long, completed: Boolean, today: LocalDate) = database.withTransaction {
        val task = taskDao.getTask(taskId) ?: return@withTransaction
        taskDao.update(
            task.copy(
                isCompleted = completed,
                completedAt = if (completed) System.currentTimeMillis() else null,
                historyGroupId = if (completed) historyGroupDao.getByMatchTitle(task.title)?.id else task.historyGroupId,
                updatedAt = System.currentTimeMillis()
            )
        )
        val template = task.recurrenceTemplateId?.let { recurrenceDao.getById(it) } ?: return@withTransaction
        val kind = RecurrenceKind.valueOf(template.kind)
        if (!RecurrenceRules.isFixed(kind)) {
            if (!completed) {
                taskDao.deletePendingGeneratedFrom(task.id)
            } else if (template.isActive) {
                val nextDate = RecurrenceRules.nextRollingDate(kind, today, template.interval)
                insertOccurrence(
                    title = template.title,
                    date = nextDate,
                    isStarred = template.isStarred,
                    templateId = template.id,
                    occurrenceKey = "${template.id}:$nextDate",
                    generatedFromTaskId = task.id
                )
            }
        }
    }

    suspend fun toggleStar(taskId: Long) {
        val task = taskDao.getTask(taskId) ?: return
        taskDao.update(task.copy(isStarred = !task.isStarred, updatedAt = System.currentTimeMillis()))
    }

    suspend fun deleteTask(taskId: Long): TaskEntity? {
        val task = taskDao.getTask(taskId) ?: return null
        taskDao.delete(task)
        return task
    }

    suspend fun restoreTask(task: TaskEntity) {
        taskDao.insert(task)
    }

    suspend fun reorder(tasks: List<TaskEntity>) {
        tasks.forEachIndexed { index, task ->
            taskDao.update(task.copy(sortOrder = index.toLong(), updatedAt = System.currentTimeMillis()))
        }
    }

    suspend fun createHistoryGroup(name: String, matchTitle: String) = database.withTransaction {
        val cleanTitle = matchTitle.trim()
        if (cleanTitle.isBlank()) return@withTransaction
        val cleanName = name.trim().ifBlank { cleanTitle }
        val now = System.currentTimeMillis()
        val existing = historyGroupDao.getByMatchTitle(cleanTitle)
        val groupId = if (existing == null) {
            historyGroupDao.insert(
                HistoryGroupEntity(
                    name = cleanName,
                    matchTitle = cleanTitle,
                    createdAt = now,
                    updatedAt = now
                )
            )
        } else {
            historyGroupDao.update(existing.copy(name = cleanName, updatedAt = now))
            existing.id
        }
        taskDao.assignCompletedByTitle(cleanTitle, groupId, now)
    }

    suspend fun addTaskToHistoryGroup(taskId: Long, groupId: Long) {
        taskDao.setHistoryGroup(taskId, groupId, System.currentTimeMillis())
    }

    private suspend fun generateFixedOccurrencesThrough(template: RecurrenceTemplateEntity, today: LocalDate) {
        val start = template.lastGeneratedDate?.let { LocalDate.parse(it).plusDays(1) }
            ?: LocalDate.parse(template.startDate)
        if (start > today) return
        var cursor = start
        while (!cursor.isAfter(today)) {
            if (RecurrenceRules.dueOn(template, cursor)) {
                insertOccurrence(
                    title = template.title,
                    date = cursor,
                    isStarred = template.isStarred,
                    templateId = template.id,
                    occurrenceKey = "${template.id}:$cursor"
                )
            }
            cursor = cursor.plusDays(1)
        }
        recurrenceDao.update(template.copy(lastGeneratedDate = today.toString(), updatedAt = System.currentTimeMillis()))
    }

    private suspend fun insertOccurrence(
        title: String,
        date: LocalDate,
        isStarred: Boolean,
        templateId: Long? = null,
        occurrenceKey: String? = null,
        generatedFromTaskId: Long? = null
    ) {
        taskDao.insert(
            TaskEntity(
                title = title,
                scheduledDate = date.toString(),
                originDate = date.toString(),
                isStarred = isStarred,
                sortOrder = taskDao.maxSortOrder(date.toString()) + 1,
                recurrenceTemplateId = templateId,
                occurrenceKey = occurrenceKey,
                generatedFromTaskId = generatedFromTaskId
            )
        )
    }
}
