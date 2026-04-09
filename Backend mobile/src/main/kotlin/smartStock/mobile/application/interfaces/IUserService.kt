package smartStock.mobile.application.interfaces

import smartStock.mobile.application.dtos.UserCreateRequest
import smartStock.mobile.application.dtos.UserResponse
import smartStock.mobile.application.dtos.UserUpdateRequest
import java.util.Optional

interface IUserService {
    fun getAll(): List<UserResponse>

    fun getById(id: Int): Optional<UserResponse>

    fun create(request: UserCreateRequest): UserResponse

    fun update(
        id: Int,
        request: UserUpdateRequest,
    ): UserResponse

    fun delete(id: Int)

    fun activate(id: Int)

    fun deactivate(id: Int)
}
