package com.cardcue.app.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

class UserPreferencesRepository(private val context: Context) {
    private val IS_DARK_MODE = booleanPreferencesKey("is_dark_mode")
    private val USE_DYNAMIC_COLORS = booleanPreferencesKey("use_dynamic_colors")
    private val IS_BIOMETRIC_ENABLED = booleanPreferencesKey("is_biometric_enabled")
    private val REMINDER_TIME = longPreferencesKey("reminder_time") // Minutes from midnight
    private val MONTHLY_SALARY = androidx.datastore.preferences.core.doublePreferencesKey("monthly_salary")
    private val PAYDAY = androidx.datastore.preferences.core.intPreferencesKey("payday")

    val isDarkMode: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[IS_DARK_MODE] ?: false
    }

    val useDynamicColors: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[USE_DYNAMIC_COLORS] ?: true
    }

    val isBiometricEnabled: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[IS_BIOMETRIC_ENABLED] ?: false
    }

    val reminderTime: Flow<Long> = context.dataStore.data.map { preferences ->
        preferences[REMINDER_TIME] ?: 540L // Default 9:00 AM (9*60)
    }

    val monthlySalary: Flow<Double> = context.dataStore.data.map { preferences ->
        preferences[MONTHLY_SALARY] ?: 0.0
    }

    val payday: Flow<Int> = context.dataStore.data.map { preferences ->
        preferences[PAYDAY] ?: 1 // Default to 1st of the month
    }

    suspend fun setDarkMode(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[IS_DARK_MODE] = enabled
        }
    }

    suspend fun setDynamicColors(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[USE_DYNAMIC_COLORS] = enabled
        }
    }

    suspend fun setBiometricEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[IS_BIOMETRIC_ENABLED] = enabled
        }
    }

    suspend fun setReminderTime(minutesFromMidnight: Long) {
        context.dataStore.edit { preferences ->
            preferences[REMINDER_TIME] = minutesFromMidnight
        }
    }

    suspend fun setMonthlySalary(salary: Double) {
        context.dataStore.edit { preferences ->
            preferences[MONTHLY_SALARY] = salary
        }
    }

    suspend fun setPayday(day: Int) {
        context.dataStore.edit { preferences ->
            preferences[PAYDAY] = day
        }
    }
}
