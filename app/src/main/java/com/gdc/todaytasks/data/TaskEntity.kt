package com.gdc.todaytasks.data

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "tasks",
    indices = [Index(value = ["occurrenceKey"], unique = true)]
)
data class TaskEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val scheduledDate: String,
    val originDate: String,
    val isCompleted: Boolean = false,
    val completedAt: Long? = null,
    val isStarred: Boolean = false,
    val sortOrder: Long = 0,
    val isCarried: Boolean = false,
    val recurrenceTemplateId: Long? = null,
    val occurrenceKey: String? = null,
    val generatedFromTaskId: Long? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)
