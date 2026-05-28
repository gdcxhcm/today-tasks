package com.gdc.todaytasks.data

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class RecurrenceKind {
    FIXED_DAILY,
    FIXED_WEEKLY,
    ROLLING_DAYS,
    ROLLING_WEEKS
}

@Entity(tableName = "recurrence_templates")
data class RecurrenceTemplateEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val isStarred: Boolean,
    val kind: String,
    val interval: Int = 1,
    val weekdays: String = "",
    val startDate: String,
    val lastGeneratedDate: String? = null,
    val isActive: Boolean = true,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)
