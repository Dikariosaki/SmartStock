using SmartStock.Domain.Entities;

namespace SmartStock.Application.Interfaces;

public interface IMovimientoRepository : IRepository<Movimiento>
{
    // Extensiones potenciales: listar por inventario/orden/usuario
}