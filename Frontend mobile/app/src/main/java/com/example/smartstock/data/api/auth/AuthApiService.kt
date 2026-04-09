package com.example.smartstock.data.api

import com.example.smartstock.data.model.AuthLoginRequestDto
import com.example.smartstock.data.model.AuthLoginResponseDto
import retrofit2.http.Body
import retrofit2.http.POST

interface AuthApiService {
    @POST("api/auth/login")
    suspend fun login(@Body request: AuthLoginRequestDto): AuthLoginResponseDto
}
