using SmartStock.Application.DTOs;
using SmartStock.Application.DTOs.Common;

namespace SmartStock.Application.Interfaces
{
    public interface IProveedorService
    {
        Task<IEnumerable<ProveedorResponse>> GetAllAsync();
        Task<PagedResponse<ProveedorResponse>> GetPagedAsync(PagedRequest request);
        Task<ProveedorResponse?> GetByIdAsync(int id);
        Task<IEnumerable<ProveedorResponse>> GetByUsuarioIdAsync(int usuarioId);
        Task<ProveedorResponse> CreateAsync(ProveedorCreateRequest request);
        Task UpdateAsync(int id, ProveedorUpdateRequest request);
        Task DeleteAsync(int id);
    }
}
