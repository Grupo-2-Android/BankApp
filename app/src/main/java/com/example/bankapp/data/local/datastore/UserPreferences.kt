package com.example.bankapp.data.local.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_prefs")

class UserPreferences(private val context: Context) {

    companion object {
        val USER_ID = stringPreferencesKey("user_id")
        val USER_NAME = stringPreferencesKey("user_name")
    }

    val userId: Flow<String?> = context.dataStore.data
        .map { preferences -> preferences[USER_ID] }

    val userName: Flow<String?> = context.dataStore.data
        .map { preferences -> preferences[USER_NAME] }

    suspend fun saveUser(id: String, name: String) {
        context.dataStore.edit { preferences ->
            preferences[USER_ID] = id
            preferences[USER_NAME] = name
        }
    }

    suspend fun clear() {
        context.dataStore.edit { preferences ->
            preferences.clear()
        }
    }
}
