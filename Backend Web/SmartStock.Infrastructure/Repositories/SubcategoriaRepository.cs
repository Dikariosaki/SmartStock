using Microsoft.EntityFrameworkCore;
using SmartStock.Application.Interfaces;
using SmartStock.Domain.Entities;
using SmartStock.Infrastructure.Data;

namespace SmartStock.Infrastructure.Repositories
{
    public class SubcategoriaRepository : Repository<Subcategoria>, ISubcategoriaRepository
    {
        private readonly SmartStockDbContext _context;
        private readonly DbSet<Subcategoria> _dbSet;

        public SubcategoriaRepository(SmartStockDbContext context) : base(context)
        {
            _context = context;
            _dbSet = _context.Subcategorias;
        }

        public async Task<bool> NombreExisteEnCategoriaAsync(int categoriaId, string nombre, int? excludeId = null)
        {
            return await _dbSet.AnyAsync(s => s.CategoriaId == categoriaId && s.Nombre == nombre && (!excludeId.HasValue || s.SubcategoriaId != excludeId.Value));
        }

        public async Task<IEnumerable<Subcategoria>> GetByCategoriaAsync(int categoriaId)
        {
            return await _dbSet.Where(s => s.CategoriaId == categoriaId).ToListAsync();
        }
    }
}