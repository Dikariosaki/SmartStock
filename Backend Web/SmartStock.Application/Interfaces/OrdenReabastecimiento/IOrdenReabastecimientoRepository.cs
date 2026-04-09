using SmartStock.Domain.Entities;

namespace SmartStock.Application.Interfaces;

public interface IOrdenReabastecimientoRepository : IRepository<OrdenReabastecimiento>
{
    Task<IEnumerable<OrdenReabastecimiento>> GetByProveedorAsync(int proveedorId);
}