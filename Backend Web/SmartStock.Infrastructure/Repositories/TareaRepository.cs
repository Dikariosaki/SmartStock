using Microsoft.EntityFrameworkCore;
using SmartStock.Application.Interfaces;
using SmartStock.Domain.Entities;
using SmartStock.Infrastructure.Data;

namespace SmartStock.Infrastructure.Repositories
{
    public class TareaRepository : Repository<Tarea>, ITareaRepository
    {
        private readonly SmartStockDbContext _context;
        private readonly DbSet<Tarea> _dbSet;

        public TareaRepository(SmartStockDbContext context) : base(context)
        {
            _context = context;
            _dbSet = _context.Tareas;
        }
    }
}