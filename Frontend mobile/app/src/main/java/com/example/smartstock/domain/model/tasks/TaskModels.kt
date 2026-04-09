package com.example.smartstock.domain.model

data class Task(
    val id: Int,
    val title: String,
    val description: String?,
    val assignedTo: Int?,
    val assignedToName: String?,
    val createdAt: String,
    val finishedAt: String?,
    val status: Boolean
)

data class CreateTaskRequest(
    val title: String,
    val description: String?,
    val assignedTo: Int?
)

data class UpdateTaskRequest(
    val title: String,
    val description: String?,
    val assignedTo: Int?,
    val status: Boolean
)
