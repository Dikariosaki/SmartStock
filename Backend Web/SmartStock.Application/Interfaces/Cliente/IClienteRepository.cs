using SmartStock.Domain.Entities;

namespace SmartStock.Application.Interfaces;

public interface IClienteRepository : IRepository<Cliente>
{
    Task<IEnumerable<Cliente>> GetByUsuarioAsync(int usuarioId);
}