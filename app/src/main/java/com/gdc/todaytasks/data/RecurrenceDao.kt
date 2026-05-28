package com.gdc.todaytasks.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update

@Dao
interface RecurrenceDao {
    @Query("SELECT * FROM recurrence_templates WHERE isActive = 1")
    suspend fun activeTemplates(): List<RecurrenceTemplateEntity>

    @Query("SELECT * FROM recurrence_templates WHERE id = :id LIMIT 1")
    suspend fun getById(id: Long): RecurrenceTemplateEntity?

    @Insert
    suspend fun insert(template: RecurrenceTemplateEntity): Long

    @Update
    suspend fun update(template: RecurrenceTemplateEntity)
}
