using System.Collections.Generic;
using System.Threading.Tasks;
using SmartStock.Domain.Entities;

namespace SmartStock.Application.Interfaces;

public interface IReporteTareaRepository
{
    Task<ReporteTarea?> GetByIdsAsync(int reporteId, int tareaId);
    Task<IReadOnlyList<ReporteTarea>> ListByReporteAsync(int reporteId);
    Task<IReadOnlyList<ReporteTarea>> ListByTareaAsync(int tareaId);
    Task<ReporteTarea> AddAsync(ReporteTarea entity);
    Task DeleteAsync(ReporteTarea entity);
}