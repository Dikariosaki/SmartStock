using SmartStock.Application.DTOs;
using SmartStock.Domain.Entities;

namespace SmartStock.Application.Interfaces
{
    public interface IAuthService
    {
        Task<Usuario?> AuthenticateAsync(AuthLoginRequest request);
    }
}
