using SmartStock.Domain.Entities;

namespace SmartStock.Application.Interfaces;

public interface ITareaRepository : IRepository<Tarea>
{
    // Extensiones potenciales: listar por asignado_a
}