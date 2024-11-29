package com.gruwier.ktordemo

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map


class AppPreferencesDataStore(
    private val context: Context,
) {
    private val Context.appPreferencesDataStore: DataStore<Preferences> by preferencesDataStore("settings")
    private val environmentKey = stringPreferencesKey("environment")

    suspend fun getEnvironment(): Environment = context.appPreferencesDataStore.data.map { preferences ->
        preferences[environmentKey]?.let {
            Environment.valueOf(it)
        } ?: Environment.PROD
    }.first()

    suspend fun updateEnvironment(environment: Environment) {
        context.appPreferencesDataStore.edit { preferences ->
            preferences[environmentKey] = environment.name
        }
    }

    suspend fun deleteAllStorage() {
        context.appPreferencesDataStore.edit { preferences ->
            preferences.clear()
        }
    }
}