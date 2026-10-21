package com.growera.app.data

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

class SessionStore(context: Context) {
    private val prefs = runCatching {
        val masterKey = MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()
        EncryptedSharedPreferences.create(
            context,
            "growera_secure_session",
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }.getOrElse {
        // If secure storage cannot be initialized, don't silently fall back to plaintext storage.
        null
    }

    fun token(): String? = prefs?.getString("session_token", null)
    fun saveToken(token: String) { prefs?.edit()?.putString("session_token", token)?.apply() }
    fun clear() { prefs?.edit()?.clear()?.apply() }
    fun isAvailable(): Boolean = prefs != null
}
