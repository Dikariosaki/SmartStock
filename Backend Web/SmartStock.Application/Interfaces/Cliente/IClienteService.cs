using SmartStock.Application.DTOs;
using SmartStock.Application.DTOs.Common;

namespace SmartStock.Application.Interfaces
{
    public interface IClienteService
    {
        Task<IEnumerable<ClienteResponse>> GetAllAsync();
        Task<PagedResponse<ClienteResponse>> GetPagedAsync(PagedRequest request);
        Task<ClienteResponse?> GetByIdAsync(int id);
        Task<IEnumerable<ClienteResponse>> GetByUsuarioIdAsync(int usuarioId);
        Task<ClienteResponse> CreateAsync(ClienteCreateRequest request);
        Task UpdateAsync(int id, ClienteUpdateRequest request);
        Task DeleteAsync(int id);
    }
}
