package com.loki.bottomnavigationbar

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class StoreUserEmail(private val context: Context) {

    companion object{
        private val  Context.dataStore: DataStore<Preferences> by preferencesDataStore("")
        val USER_TIME = stringPreferencesKey("user_time")
        val USER_NAME = stringPreferencesKey("user_name")
    }

    val getTime: Flow<String?> = context.dataStore.data
        .map { Preferences ->
            Preferences[USER_TIME]
        }

    suspend fun saveTime(name: String){
        context.dataStore.edit { Preferences ->
            Preferences[USER_TIME] = name
        }
    }

    val getName: Flow<String?> = context.dataStore.data
        .map { Preferences ->
            Preferences[USER_NAME]
        }

    suspend fun saveName(name: String){
        context.dataStore.edit { Preferences ->
            Preferences[USER_NAME] = name
        }
    }

}