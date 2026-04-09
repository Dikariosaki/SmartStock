using Microsoft.EntityFrameworkCore;
using SmartStock.Application.Interfaces;
using SmartStock.Domain.Entities;
using SmartStock.Infrastructure.Data;

namespace SmartStock.Infrastructure.Repositories
{
    public class ProveedorRepository : Repository<Proveedor>, IProveedorRepository
    {
        private readonly SmartStockDbContext _context;
        private readonly DbSet<Proveedor> _dbSet;

        public ProveedorRepository(SmartStockDbContext context) : base(context)
        {
            _context = context;
            _dbSet = _context.Proveedores;
        }

        public async Task<IReadOnlyList<Proveedor>> ListByUsuarioIdAsync(int usuarioId)
        {
            return await _dbSet
                .Include(p => p.Usuario)
                .Where(p => p.UsuarioId == usuarioId)
                .ToListAsync();
        }

        // Override base methods to include Usuario information
        public override async Task<Proveedor?> GetByIdAsync(object id)
        {
            return await _dbSet
                .Include(p => p.Usuario)
                .FirstOrDefaultAsync(p => p.ProveedorId == (int)id);
        }

        public override async Task<IReadOnlyList<Proveedor>> ListAsync(System.Linq.Expressions.Expression<Func<Proveedor, bool>>? predicate = null)
        {
            IQueryable<Proveedor> query = _dbSet.Include(p => p.Usuario);

            if (predicate != null)
                query = query.Where(predicate);

            return await query.ToListAsync();
        }

        public override async Task<(IEnumerable<Proveedor> Items, int TotalCount)> GetPagedAsync(
            int pageNumber,
            int pageSize,
            System.Linq.Expressions.Expression<Func<Proveedor, bool>>? predicate = null,
            Func<IQueryable<Proveedor>, IOrderedQueryable<Proveedor>>? orderBy = null)
        {
            IQueryable<Proveedor> query = _dbSet.Include(p => p.Usuario);

            if (predicate != null)
                query = query.Where(predicate);

            int totalCount = await query.CountAsync();

            if (orderBy != null)
                query = orderBy(query);

            var items = await query
                .Skip((pageNumber - 1) * pageSize)
                .Take(pageSize)
                .ToListAsync();

            return (items, totalCount);
        }
    }
}