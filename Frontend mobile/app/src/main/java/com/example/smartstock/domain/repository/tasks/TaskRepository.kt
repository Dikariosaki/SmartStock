package com.example.smartstock.domain.repository

import com.example.smartstock.domain.model.CreateTaskRequest
import com.example.smartstock.domain.model.Task
import com.example.smartstock.domain.model.UpdateTaskRequest

interface TaskRepository {
    suspend fun getTasks(): List<Task>
    suspend fun getTaskById(id: Int): Task
    suspend fun getTasksByUser(userId: Int): List<Task>
    suspend fun createTask(request: CreateTaskRequest): Task
    suspend fun updateTask(id: Int, request: UpdateTaskRequest): Task
    suspend fun activateTask(id: Int)
    suspend fun deactivateTask(id: Int)
    suspend fun deleteTask(id: Int)
}
