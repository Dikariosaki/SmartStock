using SmartStock.Domain.Entities;

namespace SmartStock.Application.Interfaces;

public interface IRolRepository : IRepository<Rol>
{
    Task<bool> NombreExisteAsync(string nombre, int? excludeId = null);
}