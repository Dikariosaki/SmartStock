package smartStock.mobile.infrastructure.security

import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter

@Component
class JwtAuthenticationFilter(
    private val jwtService: JwtService,
) : OncePerRequestFilter() {
    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain,
    ) {
        val authHeader = request.getHeader("Authorization")
        val token =
            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                authHeader.removePrefix("Bearer ").trim()
            } else {
                null
            }

        if (!token.isNullOrBlank() && SecurityContextHolder.getContext().authentication == null && jwtService.isTokenValid(token)) {
            val userId = jwtService.extractUserId(token)
            val email = jwtService.extractEmail(token)
            val roleName = jwtService.extractRole(token)
            val roleId = jwtService.extractRoleId(token)
            val name = jwtService.extractName(token)

            if (userId != null && !email.isNullOrBlank() && !roleName.isNullOrBlank() && roleId != null) {
                val normalizedRole = roleName.lowercase()
                val principal =
                    AuthenticatedUser(
                        id = userId,
                        email = email,
                        roleId = roleId,
                        roleName = normalizedRole,
                        name = name.orEmpty(),
                    )

                val authorities = listOf(SimpleGrantedAuthority("ROLE_${normalizedRole}"))
                val authentication = UsernamePasswordAuthenticationToken(principal, null, authorities)
                SecurityContextHolder.getContext().authentication = authentication
            }
        }

        filterChain.doFilter(request, response)
    }
}
