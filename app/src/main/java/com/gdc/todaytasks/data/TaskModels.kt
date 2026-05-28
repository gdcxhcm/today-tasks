package com.gdc.todaytasks.data

import java.time.DayOfWeek
import java.time.LocalDate

data class RecurrenceDraft(
    val kind: RecurrenceKind,
    val interval: Int = 1,
    val weekdays: Set<DayOfWeek> = emptySet()
)

data class TaskDraft(
    val title: String,
    val date: LocalDate,
    val isStarred: Boolean,
    val recurrence: RecurrenceDraft? = null
)

data class HistoryGroupDraft(
    val name: String,
    val matchTitle: String
)

object RecurrenceRules {
    fun isFixed(kind: RecurrenceKind): Boolean =
        kind == RecurrenceKind.FIXED_DAILY || kind == RecurrenceKind.FIXED_WEEKLY

    fun weekdaysToText(days: Set<DayOfWeek>): String =
        days.sortedBy { it.value }.joinToString(",") { it.value.toString() }

    fun textToWeekdays(value: String): Set<DayOfWeek> =
        value.split(",")
            .mapNotNull { it.toIntOrNull() }
            .map { DayOfWeek.of(it) }
            .toSet()

    fun dueOn(template: RecurrenceTemplateEntity, date: LocalDate): Boolean {
        if (date < LocalDate.parse(template.startDate)) return false
        return when (RecurrenceKind.valueOf(template.kind)) {
            RecurrenceKind.FIXED_DAILY -> true
            RecurrenceKind.FIXED_WEEKLY -> date.dayOfWeek in textToWeekdays(template.weekdays)
            else -> false
        }
    }

    fun firstFixedOccurrence(draft: TaskDraft): LocalDate {
        val recurrence = requireNotNull(draft.recurrence)
        if (recurrence.kind == RecurrenceKind.FIXED_DAILY) return draft.date
        val weekdays = recurrence.weekdays.ifEmpty { setOf(draft.date.dayOfWeek) }
        return (0L..6L)
            .map { draft.date.plusDays(it) }
            .first { it.dayOfWeek in weekdays }
    }

    fun nextRollingDate(kind: RecurrenceKind, completedOn: LocalDate, interval: Int): LocalDate =
        when (kind) {
            RecurrenceKind.ROLLING_DAYS -> completedOn.plusDays(interval.coerceAtLeast(1).toLong())
            RecurrenceKind.ROLLING_WEEKS -> completedOn.plusWeeks(interval.coerceAtLeast(1).toLong())
            else -> error("Only rolling recurrences use a completion-based next date.")
        }
}
