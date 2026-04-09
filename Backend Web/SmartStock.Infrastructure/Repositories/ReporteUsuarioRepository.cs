using Microsoft.EntityFrameworkCore;
using SmartStock.Application.Interfaces;
using SmartStock.Domain.Entities;
using SmartStock.Infrastructure.Data;

namespace SmartStock.Infrastructure.Repositories
{
    public class ReporteUsuarioRepository : IReporteUsuarioRepository
    {
        private readonly SmartStockDbContext _context;
        private readonly DbSet<ReporteUsuario> _dbSet;

        public ReporteUsuarioRepository(SmartStockDbContext context)
        {
            _context = context;
            _dbSet = _context.ReporteUsuarios;
        }

        public async Task<ReporteUsuario?> GetByIdsAsync(int reporteId, int usuarioId)
        {
            return await _dbSet.FindAsync(reporteId, usuarioId);
        }

        public async Task<IReadOnlyList<ReporteUsuario>> ListByReporteAsync(int reporteId)
        {
            return await _dbSet.Where(ru => ru.ReporteId == reporteId).ToListAsync();
        }

        public async Task<IReadOnlyList<ReporteUsuario>> ListByUsuarioAsync(int usuarioId)
        {
            return await _dbSet.Where(ru => ru.UsuarioId == usuarioId).ToListAsync();
        }

        public async Task<ReporteUsuario> AddAsync(ReporteUsuario entity)
        {
            await _dbSet.AddAsync(entity);
            return entity;
        }

        public Task DeleteAsync(ReporteUsuario entity)
        {
            _dbSet.Remove(entity);
            return Task.CompletedTask;
        }
    }
}
