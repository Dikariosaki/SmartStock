using Microsoft.EntityFrameworkCore;
using SmartStock.Application.Interfaces;
using SmartStock.Domain.Entities;
using SmartStock.Infrastructure.Data;

namespace SmartStock.Infrastructure.Repositories
{
    public class ReporteRepository : Repository<Reporte>, IReporteRepository
    {
        private readonly SmartStockDbContext _context;
        private readonly DbSet<Reporte> _dbSet;

        public ReporteRepository(SmartStockDbContext context) : base(context)
        {
            _context = context;
            _dbSet = _context.Reportes;
        }
    }
}