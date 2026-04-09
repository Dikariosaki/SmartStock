using SmartStock.Application.DTOs;
using SmartStock.Application.DTOs.Common;

namespace SmartStock.Application.Interfaces
{
    public interface IUsuarioService
    {
        Task<IEnumerable<UsuarioResponse>> GetAllAsync();
        Task<PagedResponse<UsuarioResponse>> GetPagedAsync(PagedRequest request);
        Task<UsuarioResponse?> GetByIdAsync(int id);
        Task<UsuarioResponse> CreateAsync(UsuarioCreateRequest request);
        Task UpdateAsync(int id, UsuarioUpdateRequest request);
        Task DeleteAsync(int id);
        Task ActivateAsync(int id);
        Task DeactivateAsync(int id);
        Task<bool> EmailExistsAsync(string email, int? excludeId = null);
    }
}
