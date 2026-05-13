package com.example.nusa_rasa.utils

import android.content.Context
import android.content.SharedPreferences

class SessionManager(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("nusa_rasa_session", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_TOKEN   = "token"
        private const val KEY_ADMIN_ID   = "admin_id"
        private const val KEY_ADMIN_NAME = "admin_name"
        private const val KEY_ADMIN_EMAIL = "admin_email"
    }

    fun saveSession(token: String, adminId: Int, adminName: String, adminEmail: String) {
        prefs.edit()
            .putString(KEY_TOKEN, token)
            .putInt(KEY_ADMIN_ID, adminId)
            .putString(KEY_ADMIN_NAME, adminName)
            .putString(KEY_ADMIN_EMAIL, adminEmail)
            .apply()
    }

    fun getToken(): String = "Bearer ${prefs.getString(KEY_TOKEN, "") ?: ""}"

    fun getRawToken(): String = prefs.getString(KEY_TOKEN, "") ?: ""

    fun getAdminName(): String = prefs.getString(KEY_ADMIN_NAME, "Admin") ?: "Admin"

    fun getAdminEmail(): String = prefs.getString(KEY_ADMIN_EMAIL, "") ?: ""

    fun isLoggedIn(): Boolean = getRawToken().isNotEmpty()

    fun clearSession() {
        prefs.edit().clear().apply()
    }
}
