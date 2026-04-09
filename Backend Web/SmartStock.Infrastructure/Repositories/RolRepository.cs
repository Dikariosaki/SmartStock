using Microsoft.EntityFrameworkCore;
using SmartStock.Application.Interfaces;
using SmartStock.Domain.Entities;
using SmartStock.Infrastructure.Data;

namespace SmartStock.Infrastructure.Repositories
{
    public class RolRepository : Repository<Rol>, IRolRepository
    {
        private readonly SmartStockDbContext _context;
        private readonly DbSet<Rol> _dbSet;

        public RolRepository(SmartStockDbContext context) : base(context)
        {
            _context = context;
            _dbSet = _context.Roles;
        }

        public async Task<bool> NombreExisteAsync(string nombre, int? excludeId = null)
        {
            return await _dbSet.AnyAsync(r => r.Nombre == nombre && (!excludeId.HasValue || r.RolId != excludeId.Value));
        }
    }
}