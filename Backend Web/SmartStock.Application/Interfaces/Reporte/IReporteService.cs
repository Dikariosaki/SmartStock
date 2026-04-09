using SmartStock.Application.DTOs;
using SmartStock.Application.DTOs.Common;

namespace SmartStock.Application.Interfaces
{
    public interface IReporteService
    {
        // Reportes
        Task<IEnumerable<ReporteResponse>> GetAllAsync();
        Task<PagedResponse<ReporteResponse>> GetPagedAsync(PagedRequest request);
        Task<ReporteResponse?> GetByIdAsync(int id);
        Task<ReporteEvidenciaImagenContenido?> GetEvidenceImageAsync(int id, int imageIndex);
        Task<ReporteResponse> CreateAsync(ReporteCreateRequest request);
        Task UpdateAsync(int id, ReporteUpdateRequest request);
        Task DeleteAsync(int id);
        Task ActivateAsync(int id);
        Task DeactivateAsync(int id);

        // ReporteMovimientos
        Task<IEnumerable<ReporteMovimientoResponse>> GetMovimientosByReporteAsync(int reporteId);
        Task<IEnumerable<ReporteMovimientoResponse>> GetReportesByMovimientoAsync(int movimientoId);
        Task<ReporteMovimientoResponse> AddMovimientoAsync(ReporteMovimientoCreateRequest request);
        Task RemoveMovimientoAsync(int reporteId, int movimientoId);

        // ReporteTareas
        Task<IEnumerable<ReporteTareaResponse>> GetTareasByReporteAsync(int reporteId);
        Task<IEnumerable<ReporteTareaResponse>> GetReportesByTareaAsync(int tareaId);
        Task<ReporteTareaResponse> AddTareaAsync(ReporteTareaCreateRequest request);
        Task RemoveTareaAsync(int reporteId, int tareaId);

        // ReporteUsuarios
        Task<IEnumerable<ReporteUsuarioResponse>> GetUsuariosByReporteAsync(int reporteId);
        Task<IEnumerable<ReporteUsuarioResponse>> GetReportesByUsuarioAsync(int usuarioId);
        Task<ReporteUsuarioResponse> AddUsuarioAsync(ReporteUsuarioCreateRequest request);
        Task RemoveUsuarioAsync(int reporteId, int usuarioId);
    }
}
