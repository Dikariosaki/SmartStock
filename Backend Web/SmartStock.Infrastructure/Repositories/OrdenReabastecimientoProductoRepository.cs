using Microsoft.EntityFrameworkCore;
using SmartStock.Application.Interfaces;
using SmartStock.Domain.Entities;
using SmartStock.Infrastructure.Data;

namespace SmartStock.Infrastructure.Repositories
{
    public class OrdenReabastecimientoProductoRepository : IOrdenReabastecimientoProductoRepository
    {
        private readonly SmartStockDbContext _context;
        private readonly DbSet<OrdenReabastecimientoProducto> _dbSet;

        public OrdenReabastecimientoProductoRepository(SmartStockDbContext context)
        {
            _context = context;
            _dbSet = _context.OrdenesReabastecimientoProductos;
        }

        public async Task<OrdenReabastecimientoProducto?> GetByIdsAsync(int ordenId, int productoId)
        {
            return await _dbSet.FindAsync(ordenId, productoId);
        }

        public async Task<IReadOnlyList<OrdenReabastecimientoProducto>> ListByOrdenAsync(int ordenId)
        {
            return await _dbSet.Where(op => op.OrdenId == ordenId).ToListAsync();
        }

        public async Task<OrdenReabastecimientoProducto> AddAsync(OrdenReabastecimientoProducto entity)
        {
            await _dbSet.AddAsync(entity);
            return entity;
        }

        public Task UpdateAsync(OrdenReabastecimientoProducto entity)
        {
            _dbSet.Update(entity);
            return Task.CompletedTask;
        }

        public Task DeleteAsync(OrdenReabastecimientoProducto entity)
        {
            _dbSet.Remove(entity);
            return Task.CompletedTask;
        }
    }
}
