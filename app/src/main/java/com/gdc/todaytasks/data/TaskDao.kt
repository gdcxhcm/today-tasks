package com.gdc.todaytasks.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface TaskDao {
    @Query("SELECT * FROM tasks WHERE scheduledDate = :date ORDER BY isCompleted ASC, isStarred DESC, sortOrder ASC, createdAt ASC")
    fun observeToday(date: String): Flow<List<TaskEntity>>

    @Query("SELECT * FROM tasks WHERE scheduledDate > :date AND isCompleted = 0 ORDER BY scheduledDate ASC, isStarred DESC, sortOrder ASC")
    fun observeFuture(date: String): Flow<List<TaskEntity>>

    @Query("SELECT * FROM tasks WHERE isCompleted = 1 ORDER BY completedAt DESC, scheduledDate DESC")
    fun observeHistory(): Flow<List<TaskEntity>>

    @Query("SELECT * FROM tasks WHERE historyGroupId = :groupId AND isCompleted = 1 ORDER BY completedAt DESC")
    fun observeCompletedInGroup(groupId: Long): Flow<List<TaskEntity>>

    @Query("SELECT COUNT(*) FROM tasks WHERE scheduledDate = :date AND isCompleted = 0")
    fun observeIncompleteCount(date: String): Flow<Int>

    @Query("SELECT * FROM tasks WHERE scheduledDate = :date AND isCompleted = 0 ORDER BY isStarred DESC, sortOrder ASC, createdAt ASC LIMIT :limit")
    suspend fun currentIncompleteTasks(date: String, limit: Int): List<TaskEntity>

    @Query("SELECT COUNT(*) FROM tasks WHERE scheduledDate = :date AND isCompleted = 0")
    suspend fun currentIncompleteCount(date: String): Int

    @Query("SELECT * FROM tasks WHERE id = :id LIMIT 1")
    suspend fun getTask(id: Long): TaskEntity?

    @Query("SELECT COALESCE(MAX(sortOrder), 0) FROM tasks WHERE scheduledDate = :date")
    suspend fun maxSortOrder(date: String): Long

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(task: TaskEntity): Long

    @Update
    suspend fun update(task: TaskEntity)

    @Delete
    suspend fun delete(task: TaskEntity)

    @Query("DELETE FROM tasks WHERE generatedFromTaskId = :taskId AND isCompleted = 0")
    suspend fun deletePendingGeneratedFrom(taskId: Long)

    @Query("UPDATE tasks SET scheduledDate = :today, isCarried = 1, updatedAt = :now WHERE scheduledDate < :today AND isCompleted = 0")
    suspend fun carryIncompleteInto(today: String, now: Long)

    @Query("UPDATE tasks SET historyGroupId = :groupId, updatedAt = :now WHERE id = :taskId")
    suspend fun setHistoryGroup(taskId: Long, groupId: Long?, now: Long)

    @Query("UPDATE tasks SET historyGroupId = :groupId, updatedAt = :now WHERE isCompleted = 1 AND title = :title")
    suspend fun assignCompletedByTitle(title: String, groupId: Long, now: Long)
}
