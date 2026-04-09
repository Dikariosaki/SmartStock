using SmartStock.Application.DTOs;
using SmartStock.Application.Helpers;
using SmartStock.Application.Interfaces;
using SmartStock.Domain.Entities;

namespace SmartStock.Application.Services
{
    public class AuthService : IAuthService
    {
        private readonly IUsuarioRepository _usuarioRepository;
        private readonly IUnitOfWork _uow;

        public AuthService(IUsuarioRepository usuarioRepository, IUnitOfWork uow)
        {
            _usuarioRepository = usuarioRepository;
            _uow = uow;
        }

        public async Task<Usuario?> AuthenticateAsync(AuthLoginRequest request)
        {
            var usuario = await _usuarioRepository.GetByEmailAsync(request.Email);
            if (usuario == null || !usuario.Estado)
                return null;

            var ok = false;
            // Primero intenta verificar bcrypt
            if (!string.IsNullOrEmpty(usuario.PasswordHash) && usuario.PasswordHash.StartsWith("$2"))
            {
                ok = Hashing.BcryptVerify(request.Password, usuario.PasswordHash);
            }
            else
            {
                // Compatibilidad: si coincide MD5, rehash con bcrypt y actualiza
                var md5 = Hashing.Md5Hex(request.Password);
                if (string.Equals(md5, usuario.PasswordHash, StringComparison.OrdinalIgnoreCase))
                {
                    ok = true;
                    usuario.PasswordHash = Hashing.BcryptHash(request.Password);
                    await _usuarioRepository.UpdateAsync(usuario);
                    await _uow.CommitAsync();
                }
            }

            return ok ? usuario : null;
        }
    }
}
