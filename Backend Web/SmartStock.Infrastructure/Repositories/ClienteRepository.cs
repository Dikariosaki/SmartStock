using Microsoft.EntityFrameworkCore;
using SmartStock.Application.Interfaces;
using SmartStock.Domain.Entities;
using SmartStock.Infrastructure.Data;

namespace SmartStock.Infrastructure.Repositories
{
    public class ClienteRepository : Repository<Cliente>, IClienteRepository
    {
        private readonly SmartStockDbContext _context;
        private readonly DbSet<Cliente> _dbSet;

        public ClienteRepository(SmartStockDbContext context) : base(context)
        {
            _context = context;
            _dbSet = _context.Clientes;
        }

        public async Task<IEnumerable<Cliente>> GetByUsuarioAsync(int usuarioId)
        {
            return await _dbSet
                .Include(c => c.Usuario)
                .Where(c => c.UsuarioId == usuarioId)
                .ToListAsync();
        }

        public override async Task<Cliente?> GetByIdAsync(object id)
        {
            return await _dbSet
                .Include(c => c.Usuario)
                .FirstOrDefaultAsync(c => c.ClienteId == (int)id);
        }

        public override async Task<IReadOnlyList<Cliente>> ListAsync(System.Linq.Expressions.Expression<Func<Cliente, bool>>? predicate = null)
        {
            IQueryable<Cliente> query = _dbSet.Include(c => c.Usuario);
            if (predicate != null)
                query = query.Where(predicate);
            return await query.ToListAsync();
        }

        public override async Task<(IEnumerable<Cliente> Items, int TotalCount)> GetPagedAsync(
            int pageNumber,
            int pageSize,
            System.Linq.Expressions.Expression<Func<Cliente, bool>>? predicate = null,
            Func<IQueryable<Cliente>, IOrderedQueryable<Cliente>>? orderBy = null)
        {
            IQueryable<Cliente> query = _dbSet.Include(c => c.Usuario);

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