using SmartStock.Application.DTOs;
using SmartStock.Application.DTOs.Common;

namespace SmartStock.Application.Interfaces
{
    public interface ISubcategoriaService
    {
        Task<IEnumerable<SubcategoriaResponse>> GetAllAsync();
        Task<PagedResponse<SubcategoriaResponse>> GetPagedAsync(PagedRequest request);
        Task<SubcategoriaResponse?> GetByIdAsync(int id);
        Task<IEnumerable<SubcategoriaResponse>> GetByCategoriaIdAsync(int categoriaId);
        Task<SubcategoriaResponse> CreateAsync(SubcategoriaCreateRequest request);
        Task UpdateAsync(int id, SubcategoriaUpdateRequest request);
        Task ActivarAsync(int id);
        Task DesactivarAsync(int id);
        Task DeleteAsync(int id);
    }
}
