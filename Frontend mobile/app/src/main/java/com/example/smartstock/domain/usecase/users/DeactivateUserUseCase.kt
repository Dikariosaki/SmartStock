package com.example.smartstock.domain.usecase

import com.example.smartstock.domain.repository.UserRepository
import javax.inject.Inject

class DeactivateUserUseCase
    @Inject
    constructor(
        private val userRepository: UserRepository
    ) {
        suspend operator fun invoke(id: Int) = userRepository.deactivateUser(id)
    }
