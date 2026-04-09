using SmartStock.Application.DTOs;
using SmartStock.Application.DTOs.Common;

namespace SmartStock.Application.Interfaces
{
    public interface IMovimientoService
    {
        Task<IEnumerable<MovimientoResponse>> GetAllAsync();
        Task<PagedResponse<MovimientoResponse>> GetPagedAsync(PagedRequest request);
        Task<MovimientoResponse?> GetByIdAsync(int id);
        Task<IEnumerable<MovimientoResponse>> GetByInventarioAsync(int inventarioId);
        Task<MovimientoResponse> CreateAsync(MovimientoCreateRequest request);
        Task UpdateAsync(int id, MovimientoUpdateRequest request);
        Task DeleteAsync(int id);
        Task ActivateAsync(int id);
        Task DeactivateAsync(int id);
    }
}
