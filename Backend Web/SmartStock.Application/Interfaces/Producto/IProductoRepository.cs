using System.Threading.Tasks;
using System.Collections.Generic;
using System.Linq.Expressions;
using SmartStock.Domain.Entities;

namespace SmartStock.Application.Interfaces
{
    public interface IProductoRepository : IRepository<Producto>
    {
        Task<Producto?> GetByCodigoAsync(string codigo);
        Task<bool> CodigoExistsAsync(string codigo, int? excludeProductoId = null);
    }
}