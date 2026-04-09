using System.Threading.Tasks;
using System.Collections.Generic;
using SmartStock.Domain.Entities;

namespace SmartStock.Application.Interfaces
{
    public interface IProveedorRepository : IRepository<Proveedor>
    {
        Task<IReadOnlyList<Proveedor>> ListByUsuarioIdAsync(int usuarioId);
    }
}