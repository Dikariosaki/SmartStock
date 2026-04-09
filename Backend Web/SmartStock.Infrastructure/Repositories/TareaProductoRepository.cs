using Microsoft.EntityFrameworkCore;
using SmartStock.Application.Interfaces;
using SmartStock.Domain.Entities;
using SmartStock.Infrastructure.Data;

namespace SmartStock.Infrastructure.Repositories
{
    public class TareaProductoRepository : ITareaProductoRepository
    {
        private readonly SmartStockDbContext _context;
        private readonly DbSet<TareaProducto> _dbSet;

        public TareaProductoRepository(SmartStockDbContext context)
        {
            _context = context;
            _dbSet = _context.TareaProductos;
        }

        public async Task<TareaProducto?> GetByIdsAsync(int tareaId, int productoId)
        {
            return await _dbSet.FindAsync(tareaId, productoId);
        }

        public async Task<IReadOnlyList<TareaProducto>> ListByTareaAsync(int tareaId)
        {
            return await _dbSet.Where(tp => tp.TareaId == tareaId).ToListAsync();
        }

        public async Task<IReadOnlyList<TareaProducto>> ListByProductoAsync(int productoId)
        {
            return await _dbSet.Where(tp => tp.ProductoId == productoId).ToListAsync();
        }

        public async Task<TareaProducto> AddAsync(TareaProducto entity)
        {
            await _dbSet.AddAsync(entity);
            return entity;
        }

        public Task UpdateAsync(TareaProducto entity)
        {
            _dbSet.Update(entity);
            return Task.CompletedTask;
        }

        public Task DeleteAsync(TareaProducto entity)
        {
            _dbSet.Remove(entity);
            return Task.CompletedTask;
        }
    }
}
