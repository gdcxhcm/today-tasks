package com.gdc.todaytasks.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface HistoryGroupDao {
    @Query("SELECT * FROM history_groups ORDER BY updatedAt DESC, createdAt DESC")
    fun observeGroups(): Flow<List<HistoryGroupEntity>>

    @Query("SELECT * FROM history_groups WHERE matchTitle = :matchTitle LIMIT 1")
    suspend fun getByMatchTitle(matchTitle: String): HistoryGroupEntity?

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(group: HistoryGroupEntity): Long

    @Update
    suspend fun update(group: HistoryGroupEntity)
}
