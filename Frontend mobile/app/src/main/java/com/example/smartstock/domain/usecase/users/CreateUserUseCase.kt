package com.example.smartstock.domain.usecase

import com.example.smartstock.domain.model.CreateUserRequest
import com.example.smartstock.domain.model.User
import com.example.smartstock.domain.repository.UserRepository
import javax.inject.Inject

class CreateUserUseCase
    @Inject
    constructor(
        private val userRepository: UserRepository
    ) {
        suspend operator fun invoke(request: CreateUserRequest): User = userRepository.createUser(request)
    }
