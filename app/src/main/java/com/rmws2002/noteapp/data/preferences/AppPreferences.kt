package com.rmws2002.noteapp.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "app_settings")

enum class ThemeMode(val value: Int) {
    SYSTEM(0), LIGHT(1), DARK(2);

    companion object {
        fun fromInt(value: Int) = entries.firstOrNull { it.value == value } ?: SYSTEM
    }
}

class AppPreferences(private val context: Context) {
    private val themeModeKey = intPreferencesKey("theme_mode")
    private val calendarSyncEnabledKey = booleanPreferencesKey("calendar_sync_enabled")
    private val selectedCalendarIdKey = longPreferencesKey("selected_calendar_id")
    private val selectedCalendarNameKey = stringPreferencesKey("selected_calendar_name")
    private val notificationsEnabledKey = booleanPreferencesKey("notifications_enabled")
    private val defaultReminderMinutesKey = intPreferencesKey("default_reminder_minutes")

    val themeMode: Flow<ThemeMode> = context.dataStore.data.map { prefs ->
        ThemeMode.fromInt(prefs[themeModeKey] ?: ThemeMode.SYSTEM.value)
    }

    val calendarSyncEnabled: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[calendarSyncEnabledKey] ?: false
    }

    val selectedCalendarId: Flow<Long?> = context.dataStore.data.map { prefs ->
        prefs[selectedCalendarIdKey]
    }

    val selectedCalendarName: Flow<String?> = context.dataStore.data.map { prefs ->
        prefs[selectedCalendarNameKey]
    }

    val notificationsEnabled: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[notificationsEnabledKey] ?: true
    }

    val defaultReminderMinutes: Flow<Int> = context.dataStore.data.map { prefs ->
        prefs[defaultReminderMinutesKey] ?: 15
    }

    suspend fun setThemeMode(mode: ThemeMode) {
        context.dataStore.edit { prefs ->
            prefs[themeModeKey] = mode.value
        }
    }

    suspend fun setCalendarSyncEnabled(enabled: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[calendarSyncEnabledKey] = enabled
        }
    }

    suspend fun setSelectedCalendar(id: Long, name: String) {
        context.dataStore.edit { prefs ->
            prefs[selectedCalendarIdKey] = id
            prefs[selectedCalendarNameKey] = name
        }
    }

    suspend fun setNotificationsEnabled(enabled: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[notificationsEnabledKey] = enabled
        }
    }

    suspend fun setDefaultReminderMinutes(minutes: Int) {
        context.dataStore.edit { prefs ->
            prefs[defaultReminderMinutesKey] = minutes
        }
    }
}
