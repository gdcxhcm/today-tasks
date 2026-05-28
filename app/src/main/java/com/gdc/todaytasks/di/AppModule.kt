package com.gdc.todaytasks.di

import android.content.Context
import com.gdc.todaytasks.data.AppDatabase
import com.gdc.todaytasks.data.RecurrenceDao
import com.gdc.todaytasks.data.TaskDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {
    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase =
        AppDatabase.getInstance(context)

    @Provides
    fun provideTaskDao(database: AppDatabase): TaskDao = database.taskDao()

    @Provides
    fun provideRecurrenceDao(database: AppDatabase): RecurrenceDao = database.recurrenceDao()
}
