using Microsoft.EntityFrameworkCore;
using SmartStock.Application.Interfaces;
using SmartStock.Domain.Entities;
using SmartStock.Infrastructure.Data;

namespace SmartStock.Infrastructure.Repositories
{
    public class OrdenReabastecimientoRepository : Repository<OrdenReabastecimiento>, IOrdenReabastecimientoRepository
    {
        private readonly SmartStockDbContext _context;
        private readonly DbSet<OrdenReabastecimiento> _dbSet;

        public OrdenReabastecimientoRepository(SmartStockDbContext context) : base(context)
        {
            _context = context;
            _dbSet = _context.OrdenesReabastecimiento;
        }

        public async Task<IEnumerable<OrdenReabastecimiento>> GetByProveedorAsync(int proveedorId)
        {
            return await _dbSet.Where(o => o.ProveedorId == proveedorId).ToListAsync();
        }
    }
}