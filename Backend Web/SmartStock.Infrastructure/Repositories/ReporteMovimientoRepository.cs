using Microsoft.EntityFrameworkCore;
using SmartStock.Application.Interfaces;
using SmartStock.Domain.Entities;
using SmartStock.Infrastructure.Data;

namespace SmartStock.Infrastructure.Repositories
{
    public class ReporteMovimientoRepository : IReporteMovimientoRepository
    {
        private readonly SmartStockDbContext _context;
        private readonly DbSet<ReporteMovimiento> _dbSet;

        public ReporteMovimientoRepository(SmartStockDbContext context)
        {
            _context = context;
            _dbSet = _context.ReporteMovimientos;
        }

        public async Task<ReporteMovimiento?> GetByIdsAsync(int reporteId, int movimientoId)
        {
            return await _dbSet.FindAsync(reporteId, movimientoId);
        }

        public async Task<IReadOnlyList<ReporteMovimiento>> ListByReporteAsync(int reporteId)
        {
            return await _dbSet.Where(rm => rm.ReporteId == reporteId).ToListAsync();
        }

        public async Task<IReadOnlyList<ReporteMovimiento>> ListByMovimientoAsync(int movimientoId)
        {
            return await _dbSet.Where(rm => rm.MovimientoId == movimientoId).ToListAsync();
        }

        public async Task<ReporteMovimiento> AddAsync(ReporteMovimiento entity)
        {
            await _dbSet.AddAsync(entity);
            return entity;
        }

        public Task DeleteAsync(ReporteMovimiento entity)
        {
            _dbSet.Remove(entity);
            return Task.CompletedTask;
        }
    }
}
