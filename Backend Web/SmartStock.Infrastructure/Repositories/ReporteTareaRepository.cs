using Microsoft.EntityFrameworkCore;
using SmartStock.Application.Interfaces;
using SmartStock.Domain.Entities;
using SmartStock.Infrastructure.Data;

namespace SmartStock.Infrastructure.Repositories
{
    public class ReporteTareaRepository : IReporteTareaRepository
    {
        private readonly SmartStockDbContext _context;
        private readonly DbSet<ReporteTarea> _dbSet;

        public ReporteTareaRepository(SmartStockDbContext context)
        {
            _context = context;
            _dbSet = _context.ReporteTareas;
        }

        public async Task<ReporteTarea?> GetByIdsAsync(int reporteId, int tareaId)
        {
            return await _dbSet.FindAsync(reporteId, tareaId);
        }

        public async Task<IReadOnlyList<ReporteTarea>> ListByReporteAsync(int reporteId)
        {
            return await _dbSet.Where(rt => rt.ReporteId == reporteId).ToListAsync();
        }

        public async Task<IReadOnlyList<ReporteTarea>> ListByTareaAsync(int tareaId)
        {
            return await _dbSet.Where(rt => rt.TareaId == tareaId).ToListAsync();
        }

        public async Task<ReporteTarea> AddAsync(ReporteTarea entity)
        {
            await _dbSet.AddAsync(entity);
            return entity;
        }

        public Task DeleteAsync(ReporteTarea entity)
        {
            _dbSet.Remove(entity);
            return Task.CompletedTask;
        }
    }
}
