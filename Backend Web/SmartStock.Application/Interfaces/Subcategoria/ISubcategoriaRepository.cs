using SmartStock.Domain.Entities;

namespace SmartStock.Application.Interfaces;

public interface ISubcategoriaRepository : IRepository<Subcategoria>
{
    Task<bool> NombreExisteEnCategoriaAsync(int categoriaId, string nombre, int? excludeId = null);
    Task<IEnumerable<Subcategoria>> GetByCategoriaAsync(int categoriaId);
}