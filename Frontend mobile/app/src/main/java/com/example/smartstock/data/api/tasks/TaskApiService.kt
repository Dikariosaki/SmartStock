package com.example.smartstock.data.api

import com.example.smartstock.data.model.TaskCreateRequestDto
import com.example.smartstock.data.model.TaskDto
import com.example.smartstock.data.model.TaskUpdateRequestDto
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

interface TaskApiService {
    @GET("api/tareas")
    suspend fun getTasks(): List<TaskDto>

    @GET("api/tareas/{id}")
    suspend fun getTaskById(@Path("id") id: Int): TaskDto

    @GET("api/tareas/usuario/{userId}")
    suspend fun getTasksByUser(@Path("userId") userId: Int): List<TaskDto>

    @POST("api/tareas")
    suspend fun createTask(@Body request: TaskCreateRequestDto): TaskDto

    @PUT("api/tareas/{id}")
    suspend fun updateTask(
        @Path("id") id: Int,
        @Body request: TaskUpdateRequestDto
    ): TaskDto

    @PUT("api/tareas/{id}/activate")
    suspend fun activateTask(@Path("id") id: Int)

    @PUT("api/tareas/{id}/deactivate")
    suspend fun deactivateTask(@Path("id") id: Int)

    @DELETE("api/tareas/{id}")
    suspend fun deleteTask(@Path("id") id: Int)
}
