package com.gdc.todaytasks.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.DayOfWeek
import java.time.LocalDate

class RecurrenceRulesTest {
    @Test
    fun weeklyFixedTaskOnlyMatchesChosenDays() {
        val template = RecurrenceTemplateEntity(
            title = "健身",
            isStarred = false,
            kind = RecurrenceKind.FIXED_WEEKLY.name,
            weekdays = RecurrenceRules.weekdaysToText(setOf(DayOfWeek.MONDAY, DayOfWeek.THURSDAY)),
            startDate = "2026-05-25"
        )

        assertTrue(RecurrenceRules.dueOn(template, LocalDate.of(2026, 5, 25)))
        assertTrue(RecurrenceRules.dueOn(template, LocalDate.of(2026, 5, 28)))
        assertFalse(RecurrenceRules.dueOn(template, LocalDate.of(2026, 5, 27)))
    }

    @Test
    fun fixedWeeklyFirstOccurrenceMovesToNextSelectedWeekday() {
        val draft = TaskDraft(
            title = "周例会",
            date = LocalDate.of(2026, 5, 27),
            isStarred = false,
            recurrence = RecurrenceDraft(RecurrenceKind.FIXED_WEEKLY, weekdays = setOf(DayOfWeek.FRIDAY))
        )

        assertEquals(LocalDate.of(2026, 5, 29), RecurrenceRules.firstFixedOccurrence(draft))
    }

    @Test
    fun rollingTaskUsesActualCompletionDate() {
        val completedOn = LocalDate.of(2026, 5, 27)

        assertEquals(
            LocalDate.of(2026, 6, 3),
            RecurrenceRules.nextRollingDate(RecurrenceKind.ROLLING_DAYS, completedOn, 7)
        )
        assertEquals(
            LocalDate.of(2026, 6, 10),
            RecurrenceRules.nextRollingDate(RecurrenceKind.ROLLING_WEEKS, completedOn, 2)
        )
    }
}
