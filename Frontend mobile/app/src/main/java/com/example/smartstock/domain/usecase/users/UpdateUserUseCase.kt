package com.example.smartstock.domain.usecase

import com.example.smartstock.domain.model.CreateUserRequest
import com.example.smartstock.domain.model.User
import com.example.smartstock.domain.repository.UserRepository
import javax.inject.Inject

class UpdateUserUseCase
    @Inject
    constructor(
        private val userRepository: UserRepository
    ) {
        suspend operator fun invoke(
            id: Int,
            request: CreateUserRequest
        ): User = userRepository.updateUser(id, request)
    }
