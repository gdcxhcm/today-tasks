package com.gdc.todaytasks.data

import android.content.Context
import androidx.core.content.edit
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AppPreferences @Inject constructor(context: Context) {
    private val prefs = context.getSharedPreferences("today_tasks_settings", Context.MODE_PRIVATE)
    private val backgroundUriFlow = MutableStateFlow(prefs.getString(KEY_BACKGROUND_URI, null))

    val backgroundUri: StateFlow<String?> = backgroundUriFlow

    fun setBackgroundUri(uri: String?) {
        prefs.edit { putString(KEY_BACKGROUND_URI, uri) }
        backgroundUriFlow.value = uri
    }

    companion object {
        private const val KEY_BACKGROUND_URI = "background_uri"
    }
}
