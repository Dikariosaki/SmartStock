using System.Collections.Generic;
using System.Threading.Tasks;
using SmartStock.Domain.Entities;

namespace SmartStock.Application.Interfaces;

public interface IReporteUsuarioRepository
{
    Task<ReporteUsuario?> GetByIdsAsync(int reporteId, int usuarioId);
    Task<IReadOnlyList<ReporteUsuario>> ListByReporteAsync(int reporteId);
    Task<IReadOnlyList<ReporteUsuario>> ListByUsuarioAsync(int usuarioId);
    Task<ReporteUsuario> AddAsync(ReporteUsuario entity);
    Task DeleteAsync(ReporteUsuario entity);
}