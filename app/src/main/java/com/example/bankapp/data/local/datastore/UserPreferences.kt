package com.example.bankapp.data.local.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_prefs")

class UserPreferences(private val context: Context) {

    companion object {

        val USER_ID = stringPreferencesKey("user_id")
        val USER_NAME = stringPreferencesKey("user_name")

        val IS_LOGGED_IN = booleanPreferencesKey("is_logged_in")
    }

    // -------------------------
    // DADOS DO USUÁRIO
    // -------------------------

    val userId: Flow<String?> = context.dataStore.data
        .map { preferences ->
            preferences[USER_ID]
        }

    val userName: Flow<String?> = context.dataStore.data
        .map { preferences ->
            preferences[USER_NAME]
        }

    // -------------------------
    // SESSÃO (LOGIN/LOGOUT)
    // -------------------------

    val isLoggedIn: Flow<Boolean> = context.dataStore.data
        .map { preferences ->
            preferences[IS_LOGGED_IN] ?: false
        }

    // -------------------------
    // SALVAR LOGIN
    // -------------------------

    suspend fun saveUser(id: String, name: String) {
        context.dataStore.edit { preferences ->
            preferences[USER_ID] = id
            preferences[USER_NAME] = name


            preferences[IS_LOGGED_IN] = true
        }
    }

    // -------------------------
    // LOGOUT / LIMPAR SESSÃO
    // -------------------------

    suspend fun clear() {
        context.dataStore.edit { preferences ->
            preferences.clear()
        }
    }


    suspend fun logout() {
        context.dataStore.edit { preferences ->
            preferences[IS_LOGGED_IN] = false
            preferences.remove(USER_ID)
            preferences.remove(USER_NAME)
        }
    }
}