using SmartStock.Application.DTOs;
using SmartStock.Application.DTOs.Common;

namespace SmartStock.Application.Interfaces
{
    public interface IProductoService
    {
        Task<IEnumerable<ProductoResponse>> GetAllAsync();
        Task<PagedResponse<ProductoResponse>> GetPagedAsync(PagedRequest request);
        Task<ProductoResponse?> GetByIdAsync(int id);
        Task<ProductoResponse> CreateAsync(ProductoCreateRequest request);
        Task UpdateAsync(int id, ProductoUpdateRequest request);
        Task DeactivateAsync(int id);
        Task ActivateAsync(int id);
        Task DeleteAsync(int id);
    }
}
