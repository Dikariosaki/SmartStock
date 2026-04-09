package com.example.smartstock.data.session

import android.util.Base64
import javax.inject.Inject
import okhttp3.Interceptor
import okhttp3.Response
import org.json.JSONObject

class AuthTokenInterceptor
    @Inject
    constructor(
        private val sessionManager: SessionManager
    ) : Interceptor {
        override fun intercept(chain: Interceptor.Chain): Response {
            val originalRequest = chain.request()
            val isAuthLogin = originalRequest.url.encodedPath.endsWith("/api/auth/login")

            val request =
                if (isAuthLogin) {
                    originalRequest
                } else {
                    val token = sessionManager.getToken()
                    if (token.isNullOrBlank()) {
                        originalRequest
                    } else {
                        originalRequest
                            .newBuilder()
                            .addHeader("Authorization", "Bearer $token")
                            .build()
                    }
                }

            val response = chain.proceed(request)
            if (response.code == 401 && !isAuthLogin) {
                val token = sessionManager.getToken()
                if (isTokenExpired(token)) {
                    sessionManager.clearSession()
                }
            }
            return response
        }

        private fun isTokenExpired(token: String?): Boolean {
            if (token.isNullOrBlank()) return true

            return runCatching {
                val tokenParts = token.split(".")
                if (tokenParts.size < 2) return@runCatching false

                val payload =
                    String(
                        Base64.decode(
                            tokenParts[1],
                            Base64.URL_SAFE or Base64.NO_WRAP or Base64.NO_PADDING,
                        ),
                    )

                val expSeconds = JSONObject(payload).optLong("exp", 0L)
                expSeconds > 0L && (System.currentTimeMillis() / 1000L) >= expSeconds
            }.getOrDefault(false)
        }
    }
