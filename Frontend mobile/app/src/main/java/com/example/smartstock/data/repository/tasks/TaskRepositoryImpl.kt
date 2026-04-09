package com.example.smartstock.data.repository

import com.example.smartstock.data.api.TaskApiService
import com.example.smartstock.data.model.toDomain
import com.example.smartstock.data.model.toDto
import com.example.smartstock.domain.model.CreateTaskRequest
import com.example.smartstock.domain.model.Task
import com.example.smartstock.domain.model.UpdateTaskRequest
import com.example.smartstock.domain.repository.TaskRepository
import javax.inject.Inject

class TaskRepositoryImpl
    @Inject
    constructor(
        private val taskApiService: TaskApiService
    ) : TaskRepository {
        override suspend fun getTasks(): List<Task> = taskApiService.getTasks().map { it.toDomain() }

        override suspend fun getTaskById(id: Int): Task = taskApiService.getTaskById(id).toDomain()

        override suspend fun getTasksByUser(userId: Int): List<Task> = taskApiService.getTasksByUser(userId).map { it.toDomain() }

        override suspend fun createTask(request: CreateTaskRequest): Task = taskApiService.createTask(request.toDto()).toDomain()

        override suspend fun updateTask(
            id: Int,
            request: UpdateTaskRequest
        ): Task = taskApiService.updateTask(id, request.toDto()).toDomain()

        override suspend fun activateTask(id: Int) {
            taskApiService.activateTask(id)
        }

        override suspend fun deactivateTask(id: Int) {
            taskApiService.deactivateTask(id)
        }

        override suspend fun deleteTask(id: Int) {
            taskApiService.deleteTask(id)
        }
    }
