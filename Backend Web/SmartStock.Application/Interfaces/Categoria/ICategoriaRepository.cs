using System.Threading.Tasks;
using System.Collections.Generic;
using System.Linq.Expressions;
using SmartStock.Domain.Entities;

namespace SmartStock.Application.Interfaces
{
    public interface ICategoriaRepository : IRepository<Categoria>
    {
        Task<bool> NombreExistsAsync(string nombre, int? excludeCategoriaId = null);
    }
}