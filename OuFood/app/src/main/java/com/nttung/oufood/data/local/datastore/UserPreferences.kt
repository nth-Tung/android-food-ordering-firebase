package com.nttung.oufood.data.local.datastore

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore by preferencesDataStore(name = "user_preferences")

@Singleton
class UserPreferences @Inject constructor(
    @ApplicationContext private val context: Context
) {
    companion object {
        private val KEY_USER_ID = stringPreferencesKey("user_id")
        private val KEY_USER_NAME = stringPreferencesKey("user_name")
        private val KEY_USER_EMAIL = stringPreferencesKey("user_email")
        private val KEY_USER_PHONE = stringPreferencesKey("user_phone")
        private val KEY_DEFAULT_ADDRESS = stringPreferencesKey("default_address")
    }

    val userIdFlow: Flow<String?> = context.dataStore.data.map { it[KEY_USER_ID] }
    val defaultAddressFlow: Flow<String?> = context.dataStore.data.map { it[KEY_DEFAULT_ADDRESS] }

    suspend fun saveUserSession(id: String, name: String, email: String, phone: String) {
        context.dataStore.edit { prefs ->
            prefs[KEY_USER_ID] = id
            prefs[KEY_USER_NAME] = name
            prefs[KEY_USER_EMAIL] = email
            prefs[KEY_USER_PHONE] = phone
        }
    }

    suspend fun saveDefaultAddress(address: String) {
        context.dataStore.edit { prefs ->
            prefs[KEY_DEFAULT_ADDRESS] = address
        }
    }

    suspend fun clearSession() {
        context.dataStore.edit { it.clear() }
    }
}
