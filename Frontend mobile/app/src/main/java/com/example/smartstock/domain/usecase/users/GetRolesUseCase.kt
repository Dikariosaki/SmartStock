package com.example.smartstock.domain.usecase

import com.example.smartstock.domain.model.RoleOption
import com.example.smartstock.domain.repository.UserRepository
import javax.inject.Inject

class GetRolesUseCase
    @Inject
    constructor(
        private val userRepository: UserRepository
    ) {
        suspend operator fun invoke(): List<RoleOption> = userRepository.getRoles()
    }
