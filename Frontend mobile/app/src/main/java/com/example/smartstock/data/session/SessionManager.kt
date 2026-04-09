package com.example.smartstock.data.session

import android.content.Context
import android.util.Base64
import com.example.smartstock.domain.model.SessionUser
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.json.JSONObject

@Singleton
class SessionManager
    @Inject
    constructor(
        @ApplicationContext context: Context
    ) {
        private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        private val _session =
            MutableStateFlow(loadSession().also {
                if (it == null) {
                    prefs.edit().clear().apply()
                }
            })
        val session: StateFlow<SessionUser?> = _session.asStateFlow()

        fun getToken(): String? = _session.value?.token

        fun currentSession(): SessionUser? = _session.value

        fun saveSession(session: SessionUser) {
            prefs
                .edit()
                .putString(KEY_TOKEN, session.token)
                .putInt(KEY_USER_ID, session.userId)
                .putInt(KEY_ROLE_ID, session.roleId)
                .putString(KEY_ROLE_NAME, session.roleName)
                .putString(KEY_NAME, session.name)
                .putString(KEY_EMAIL, session.email)
                .apply()
            _session.value = session
        }

        fun clearSession() {
            prefs.edit().clear().apply()
            _session.value = null
        }

        private fun loadSession(): SessionUser? {
            val token = prefs.getString(KEY_TOKEN, null) ?: return null
            if (isTokenExpired(token)) return null
            val userId = prefs.getInt(KEY_USER_ID, -1)
            val roleId = prefs.getInt(KEY_ROLE_ID, -1)
            val roleName = prefs.getString(KEY_ROLE_NAME, null)
            val name = prefs.getString(KEY_NAME, null)
            val email = prefs.getString(KEY_EMAIL, null)

            if (userId <= 0 || roleId <= 0 || roleName.isNullOrBlank() || name.isNullOrBlank() || email.isNullOrBlank()) {
                return null
            }

            return SessionUser(
                token = token,
                userId = userId,
                roleId = roleId,
                roleName = roleName,
                name = name,
                email = email,
            )
        }

        private fun isTokenExpired(token: String): Boolean {
            return runCatching {
                val parts = token.split(".")
                if (parts.size != 3) return true
                val decoded = String(Base64.decode(parts[1], Base64.URL_SAFE or Base64.NO_WRAP or Base64.NO_PADDING))
                val payload = JSONObject(decoded)
                val exp = payload.optLong("exp", 0L)
                if (exp <= 0L) return true
                val nowSeconds = System.currentTimeMillis() / 1000
                exp <= nowSeconds
            }.getOrElse { true }
        }

        companion object {
            private const val PREFS_NAME = "smartstock_session"
            private const val KEY_TOKEN = "token"
            private const val KEY_USER_ID = "user_id"
            private const val KEY_ROLE_ID = "role_id"
            private const val KEY_ROLE_NAME = "role_name"
            private const val KEY_NAME = "name"
            private const val KEY_EMAIL = "email"
        }
    }
