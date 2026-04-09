using SmartStock.Application.DTOs;
using SmartStock.Application.DTOs.Common;

namespace SmartStock.Application.Interfaces
{
    public interface ITareaService
    {
        // Tareas
        Task<IEnumerable<TareaResponse>> GetAllAsync();
        Task<PagedResponse<TareaResponse>> GetPagedAsync(PagedRequest request);
        Task<TareaResponse?> GetByIdAsync(int id);
        Task<TareaResponse> CreateAsync(TareaCreateRequest request);
        Task UpdateAsync(int id, TareaUpdateRequest request);
        Task DeleteAsync(int id);
        Task ActivateAsync(int id);
        Task DeactivateAsync(int id);

        // TareaProductos
        Task<IEnumerable<TareaProductoResponse>> GetProductosByTareaAsync(int tareaId);
        Task<IEnumerable<TareaProductoResponse>> GetTareasByProductoAsync(int productoId);
        Task<TareaProductoResponse?> GetProductoByIdsAsync(int tareaId, int productoId);
        Task<TareaProductoResponse> AddProductoAsync(TareaProductoCreateRequest request);
        Task UpdateProductoAsync(int tareaId, int productoId, TareaProductoUpdateRequest request);
        Task RemoveProductoAsync(int tareaId, int productoId);
    }
}
