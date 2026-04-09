using SmartStock.Application.DTOs;
using SmartStock.Application.DTOs.Common;

namespace SmartStock.Application.Interfaces
{
    public interface ICategoriaService
    {
        Task<IEnumerable<CategoriaResponse>> GetAllAsync();
        Task<PagedResponse<CategoriaResponse>> GetPagedAsync(PagedRequest request);
        Task<CategoriaResponse?> GetByIdAsync(int id);
        Task<CategoriaResponse> CreateAsync(CategoriaCreateRequest request);
        Task UpdateAsync(int id, CategoriaUpdateRequest request);
        Task DeleteAsync(int id);
    }
}
