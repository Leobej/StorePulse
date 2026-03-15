package org.projects.storepulse.android.data

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.sessionDataStore by preferencesDataStore(name = "storepulse_session")

interface SessionStore {
    fun accessToken(): Flow<String?>
    suspend fun saveAccessToken(token: String)
    suspend fun clear()
}

class DataStoreSessionStore(
    private val context: Context
) : SessionStore {
    private companion object {
        val ACCESS_TOKEN: Preferences.Key<String> = stringPreferencesKey("access_token")
    }

    override fun accessToken(): Flow<String?> =
        context.sessionDataStore.data.map { preferences -> preferences[ACCESS_TOKEN] }

    override suspend fun saveAccessToken(token: String) {
        context.sessionDataStore.edit { preferences ->
            preferences[ACCESS_TOKEN] = token
        }
    }

    override suspend fun clear() {
        context.sessionDataStore.edit { preferences ->
            preferences.remove(ACCESS_TOKEN)
        }
    }
}
