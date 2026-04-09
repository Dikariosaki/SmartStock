using Microsoft.EntityFrameworkCore;
using SmartStock.Application.Interfaces;
using SmartStock.Domain.Entities;
using SmartStock.Infrastructure.Data;

namespace SmartStock.Infrastructure.Repositories
{
    public class UsuarioRepository : Repository<Usuario>, IUsuarioRepository
    {
        private readonly SmartStockDbContext _context;
        private readonly DbSet<Usuario> _dbSet;

        public UsuarioRepository(SmartStockDbContext context) : base(context)
        {
            _context = context;
            _dbSet = _context.Usuarios;
        }

        public async Task<Usuario?> GetByEmailAsync(string email)
        {
            return await _dbSet.Include(u => u.Rol).FirstOrDefaultAsync(u => u.Email == email);
        }

        public async Task<bool> EmailExistsAsync(string email, int? excludeUsuarioId = null)
        {
            return await _dbSet.AnyAsync(u => u.Email == email && (!excludeUsuarioId.HasValue || u.UsuarioId != excludeUsuarioId.Value));
        }
    }
}
