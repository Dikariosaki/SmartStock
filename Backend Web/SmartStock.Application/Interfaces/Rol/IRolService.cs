using SmartStock.Application.DTOs;
using SmartStock.Application.DTOs.Common;

namespace SmartStock.Application.Interfaces
{
    public interface IRolService
    {
        Task<IEnumerable<RolResponse>> GetAllAsync();
        Task<PagedResponse<RolResponse>> GetPagedAsync(PagedRequest request);
        Task<RolResponse?> GetByIdAsync(int id);
        Task<RolResponse> CreateAsync(RolCreateRequest request);
        Task UpdateAsync(int id, RolUpdateRequest request);
        Task DeleteAsync(int id);
    }
}
