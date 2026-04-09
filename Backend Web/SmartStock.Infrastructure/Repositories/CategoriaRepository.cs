using Microsoft.EntityFrameworkCore;
using SmartStock.Application.Interfaces;
using SmartStock.Domain.Entities;
using SmartStock.Infrastructure.Data;

namespace SmartStock.Infrastructure.Repositories
{
    public class CategoriaRepository : Repository<Categoria>, ICategoriaRepository
    {
        private readonly SmartStockDbContext _context;
        private readonly DbSet<Categoria> _dbSet;

        public CategoriaRepository(SmartStockDbContext context) : base(context)
        {
            _context = context;
            _dbSet = _context.Categorias;
        }

        public async Task<bool> NombreExistsAsync(string nombre, int? excludeCategoriaId = null)
        {
            return await _dbSet.AnyAsync(c => c.Nombre == nombre && (!excludeCategoriaId.HasValue || c.CategoriaId != excludeCategoriaId.Value));
        }
    }
}