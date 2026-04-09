using Microsoft.EntityFrameworkCore;
using SmartStock.Application.Interfaces;
using SmartStock.Domain.Entities;
using SmartStock.Infrastructure.Data;

namespace SmartStock.Infrastructure.Repositories
{
    public class ProductoRepository : Repository<Producto>, IProductoRepository
    {
        private readonly SmartStockDbContext _context;
        private readonly DbSet<Producto> _dbSet;

        public ProductoRepository(SmartStockDbContext context) : base(context)
        {
            _context = context;
            _dbSet = _context.Productos;
        }

        public async Task<Producto?> GetByCodigoAsync(string codigo)
        {
            return await _dbSet.FirstOrDefaultAsync(p => p.Codigo == codigo);
        }

        public async Task<bool> CodigoExistsAsync(string codigo, int? excludeProductoId = null)
        {
            return await _dbSet.AnyAsync(p => p.Codigo == codigo && (!excludeProductoId.HasValue || p.ProductoId != excludeProductoId.Value));
        }
    }
}