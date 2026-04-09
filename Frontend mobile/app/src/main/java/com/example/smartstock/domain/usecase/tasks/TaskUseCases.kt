package com.example.smartstock.domain.usecase

import com.example.smartstock.domain.model.CreateTaskRequest
import com.example.smartstock.domain.model.Task
import com.example.smartstock.domain.model.UpdateTaskRequest
import com.example.smartstock.domain.repository.TaskRepository
import javax.inject.Inject

class GetTasksUseCase @Inject constructor(private val taskRepository: TaskRepository) {
    suspend operator fun invoke(): List<Task> = taskRepository.getTasks()
}

class GetTaskByIdUseCase @Inject constructor(private val taskRepository: TaskRepository) {
    suspend operator fun invoke(id: Int): Task = taskRepository.getTaskById(id)
}

class GetTasksByUserUseCase @Inject constructor(private val taskRepository: TaskRepository) {
    suspend operator fun invoke(userId: Int): List<Task> = taskRepository.getTasksByUser(userId)
}

class CreateTaskUseCase @Inject constructor(private val taskRepository: TaskRepository) {
    suspend operator fun invoke(request: CreateTaskRequest): Task = taskRepository.createTask(request)
}

class UpdateTaskUseCase @Inject constructor(private val taskRepository: TaskRepository) {
    suspend operator fun invoke(id: Int, request: UpdateTaskRequest): Task = taskRepository.updateTask(id, request)
}

class ActivateTaskUseCase @Inject constructor(private val taskRepository: TaskRepository) {
    suspend operator fun invoke(id: Int) = taskRepository.activateTask(id)
}

class DeactivateTaskUseCase @Inject constructor(private val taskRepository: TaskRepository) {
    suspend operator fun invoke(id: Int) = taskRepository.deactivateTask(id)
}

class DeleteTaskUseCase @Inject constructor(private val taskRepository: TaskRepository) {
    suspend operator fun invoke(id: Int) = taskRepository.deleteTask(id)
}
