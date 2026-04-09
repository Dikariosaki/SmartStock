using Microsoft.EntityFrameworkCore;
using SmartStock.Application.Interfaces;
using SmartStock.Domain.Entities;
using SmartStock.Infrastructure.Data;

namespace SmartStock.Infrastructure.Repositories
{
    public class InventarioRepository : Repository<Inventario>, IInventarioRepository
    {
        private readonly SmartStockDbContext _context;
        private readonly DbSet<Inventario> _dbSet;

        public InventarioRepository(SmartStockDbContext context) : base(context)
        {
            _context = context;
            _dbSet = _context.Inventarios;
        }

        public async Task<IEnumerable<Inventario>> GetByProductoAsync(int productoId)
        {
            return await _dbSet.Where(i => i.ProductoId == productoId).ToListAsync();
        }

        public async Task<Inventario?> GetByProductoUbicacionAsync(int productoId, string ubicacion)
        {
            return await _dbSet.FirstOrDefaultAsync(i => i.ProductoId == productoId && i.Ubicacion == ubicacion);
        }

        public async Task<bool> ExisteProductoUbicacionAsync(int productoId, string ubicacion, int? excludeId = null)
        {
            return await _dbSet.AnyAsync(i => i.ProductoId == productoId && i.Ubicacion == ubicacion && (!excludeId.HasValue || i.InventarioId != excludeId.Value));
        }
    }
}