using System.Collections.Generic;
using System.Threading.Tasks;
using SmartStock.Domain.Entities;

namespace SmartStock.Application.Interfaces;

public interface ITareaProductoRepository
{
    Task<TareaProducto?> GetByIdsAsync(int tareaId, int productoId);
    Task<IReadOnlyList<TareaProducto>> ListByTareaAsync(int tareaId);
    Task<IReadOnlyList<TareaProducto>> ListByProductoAsync(int productoId);
    Task<TareaProducto> AddAsync(TareaProducto entity);
    Task UpdateAsync(TareaProducto entity);
    Task DeleteAsync(TareaProducto entity);
}