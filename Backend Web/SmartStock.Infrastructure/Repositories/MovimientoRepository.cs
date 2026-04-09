using Microsoft.EntityFrameworkCore;
using SmartStock.Application.Interfaces;
using SmartStock.Domain.Entities;
using SmartStock.Infrastructure.Data;

namespace SmartStock.Infrastructure.Repositories
{
    public class MovimientoRepository : Repository<Movimiento>, IMovimientoRepository
    {
        private readonly SmartStockDbContext _context;
        private readonly DbSet<Movimiento> _dbSet;

        public MovimientoRepository(SmartStockDbContext context) : base(context)
        {
            _context = context;
            _dbSet = _context.Movimientos;
        }
    }
}