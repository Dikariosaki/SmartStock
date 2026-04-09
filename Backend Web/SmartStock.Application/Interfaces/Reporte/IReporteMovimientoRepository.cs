using System.Collections.Generic;
using System.Threading.Tasks;
using SmartStock.Domain.Entities;

namespace SmartStock.Application.Interfaces;

public interface IReporteMovimientoRepository
{
    Task<ReporteMovimiento?> GetByIdsAsync(int reporteId, int movimientoId);
    Task<IReadOnlyList<ReporteMovimiento>> ListByReporteAsync(int reporteId);
    Task<IReadOnlyList<ReporteMovimiento>> ListByMovimientoAsync(int movimientoId);
    Task<ReporteMovimiento> AddAsync(ReporteMovimiento entity);
    Task DeleteAsync(ReporteMovimiento entity);
}