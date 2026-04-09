using SmartStock.Domain.Entities;

namespace SmartStock.Application.Interfaces
{
    public interface IUsuarioRepository : IRepository<Usuario>
    {
        Task<Usuario?> GetByEmailAsync(string email);
        Task<bool> EmailExistsAsync(string email, int? excludeUsuarioId = null);
    }
}