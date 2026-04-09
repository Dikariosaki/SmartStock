using SmartStock.Domain.Entities;

namespace SmartStock.Application.Interfaces;

public interface IInventarioRepository : IRepository<Inventario>
{
    Task<IEnumerable<Inventario>> GetByProductoAsync(int productoId);
    Task<Inventario?> GetByProductoUbicacionAsync(int productoId, string ubicacion);
    Task<bool> ExisteProductoUbicacionAsync(int productoId, string ubicacion, int? excludeId = null);
}