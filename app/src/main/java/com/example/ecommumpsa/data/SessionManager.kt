package com.example.ecommumpsa.data

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

class SessionManager(context: Context) {
    private val prefs = EncryptedSharedPreferences.create(
        context,
        "ecomm_prefs",
        MasterKey.Builder(context).setKeyScheme(MasterKey.KeyScheme.AES256_GCM).build(),
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    fun saveCredentials(username: String, password: String) {
        prefs.edit().putString("username", username).putString("password", password).apply()
    }

    fun getUsername(): String? = prefs.getString("username", null)
    fun getPassword(): String? = prefs.getString("password", null)
    fun clear() = prefs.edit().clear().apply()
}