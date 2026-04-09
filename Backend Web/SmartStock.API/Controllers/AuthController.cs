using Microsoft.AspNetCore.Mvc;
using Microsoft.AspNetCore.Authorization;
using SmartStock.Application.Interfaces;
using SmartStock.Application.DTOs;
using System.IdentityModel.Tokens.Jwt;
using Microsoft.IdentityModel.Tokens;
using System.Security.Claims;
using System.Text;

namespace SmartStock.API.Controllers
{
    [ApiController]
    [Route("api/[controller]")]
    public class AuthController : ControllerBase
    {
        private readonly IAuthService _authService;
        private readonly IConfiguration _config;

        public AuthController(IAuthService authService, IConfiguration config)
        {
            _authService = authService;
            _config = config;
        }

        [AllowAnonymous]
        [HttpPost("login")]
        public async Task<ActionResult<AuthLoginResponse>> Login([FromBody] AuthLoginRequest request)
        {
            if (!ModelState.IsValid)
                return ValidationProblem(ModelState);

            var usuario = await _authService.AuthenticateAsync(request);

            if (usuario == null)
                return Unauthorized(new { message = "Credenciales inválidas" });

            var token = GenerateJwtToken(usuario.UsuarioId, usuario.Email, usuario.RolId, usuario.Rol?.Nombre ?? "", usuario.Nombre);
            var response = new AuthLoginResponse
            {
                Token = token,
                UsuarioId = usuario.UsuarioId,
                RolId = usuario.RolId,
                Nombre = usuario.Nombre,
                Email = usuario.Email
            };
            return Ok(response);
        }

        private string GenerateJwtToken(int usuarioId, string email, int rolId, string roleName, string nombre)
        {
            var jwtSection = _config.GetSection("Jwt");
            var key = jwtSection.GetValue<string>("Key") ?? throw new InvalidOperationException("Jwt:Key no configurado");
            var issuer = jwtSection.GetValue<string>("Issuer") ?? "SmartStock";
            var audience = jwtSection.GetValue<string>("Audience") ?? "SmartStockClient";
            var expiresMinutes = jwtSection.GetValue<int>("ExpiresMinutes");
            if (expiresMinutes <= 0) expiresMinutes = 60;

            var claims = new List<Claim>
            {
                new Claim(JwtRegisteredClaimNames.Sub, usuarioId.ToString()),
                new Claim(JwtRegisteredClaimNames.Email, email),
                new Claim("rolId", rolId.ToString()),
                new Claim(ClaimTypes.Role, roleName),
                new Claim("nombre", nombre)
            };

            var securityKey = new SymmetricSecurityKey(Encoding.UTF8.GetBytes(key));
            var credentials = new SigningCredentials(securityKey, SecurityAlgorithms.HmacSha256);

            var tokenDescriptor = new JwtSecurityToken(
                issuer: issuer,
                audience: audience,
                claims: claims,
                expires: DateTime.UtcNow.AddMinutes(expiresMinutes),
                signingCredentials: credentials
            );

            return new JwtSecurityTokenHandler().WriteToken(tokenDescriptor);
        }
    }
}
