package com.example.smartstock.data.model

import com.example.smartstock.domain.model.CreateUserRequest
import com.example.smartstock.domain.model.RoleOption
import com.example.smartstock.domain.model.User

fun UserDto.toDomain(): User =
    User(
        id = usuarioId ?: 0,
        roleId = rolId,
        roleName = rolNombre,
        name = nombre,
        cedula = cedula,
        email = email,
        status = estado,
        phone = telefono
    )

fun RoleDto.toDomain(): RoleOption =
    RoleOption(
        id = rolId,
        name = nombre,
    )

fun CreateUserRequest.toDto(): CreateUserRequestDto =
    CreateUserRequestDto(
        rolId = roleId,
        nombre = name,
        cedula = cedula,
        email = email,
        password = password,
        telefono = phone,
        estado = status
    )
