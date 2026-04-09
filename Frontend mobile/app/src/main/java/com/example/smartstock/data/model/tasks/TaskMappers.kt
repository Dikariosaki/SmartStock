package com.example.smartstock.data.model

import com.example.smartstock.domain.model.CreateTaskRequest
import com.example.smartstock.domain.model.Task
import com.example.smartstock.domain.model.UpdateTaskRequest

fun TaskDto.toDomain(): Task =
    Task(
        id = tareaId ?: 0,
        title = titulo,
        description = descripcion,
        assignedTo = asignadoA,
        assignedToName = asignadoNombre,
        createdAt = fechaCreacion,
        finishedAt = fechaFin,
        status = estado
    )

fun CreateTaskRequest.toDto(): TaskCreateRequestDto =
    TaskCreateRequestDto(
        titulo = title,
        descripcion = description,
        asignadoA = assignedTo
    )

fun UpdateTaskRequest.toDto(): TaskUpdateRequestDto =
    TaskUpdateRequestDto(
        titulo = title,
        descripcion = description,
        asignadoA = assignedTo,
        estado = status
    )
