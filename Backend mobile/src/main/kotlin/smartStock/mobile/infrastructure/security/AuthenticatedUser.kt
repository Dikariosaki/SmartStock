package smartStock.mobile.infrastructure.security

data class AuthenticatedUser(
    val id: Int,
    val email: String,
    val roleId: Int,
    val roleName: String,
    val name: String,
)
