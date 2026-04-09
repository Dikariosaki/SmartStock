package com.example.smartstock.domain.usecase

import com.example.smartstock.domain.model.User
import com.example.smartstock.domain.repository.UserRepository
import javax.inject.Inject

class GetUsersUseCase
    @Inject
    constructor(
        private val userRepository: UserRepository
    ) {
        suspend operator fun invoke(): List<User> = userRepository.getUsers()
    }
