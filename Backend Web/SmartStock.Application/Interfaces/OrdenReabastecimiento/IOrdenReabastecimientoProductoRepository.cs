using System.Collections.Generic;
using System.Threading.Tasks;
using SmartStock.Domain.Entities;

namespace SmartStock.Application.Interfaces;

public interface IOrdenReabastecimientoProductoRepository
{
    Task<OrdenReabastecimientoProducto?> GetByIdsAsync(int ordenId, int productoId);
    Task<IReadOnlyList<OrdenReabastecimientoProducto>> ListByOrdenAsync(int ordenId);
    Task<OrdenReabastecimientoProducto> AddAsync(OrdenReabastecimientoProducto entity);
    Task UpdateAsync(OrdenReabastecimientoProducto entity);
    Task DeleteAsync(OrdenReabastecimientoProducto entity);
}