using SmartStock.Application.DTOs;
using SmartStock.Application.DTOs.Common;

namespace SmartStock.Application.Interfaces
{
    public interface IOrdenReabastecimientoService
    {
        Task<IEnumerable<OrdenReabastecimientoResponse>> GetAllAsync();
        Task<PagedResponse<OrdenReabastecimientoResponse>> GetPagedAsync(PagedRequest request);
        Task<OrdenReabastecimientoResponse?> GetByIdAsync(int id);
        Task<IEnumerable<OrdenReabastecimientoResponse>> GetByProveedorAsync(int proveedorId);
        Task<OrdenReabastecimientoResponse> CreateAsync(OrdenReabastecimientoCreateRequest request);
        Task UpdateAsync(int id, OrdenReabastecimientoUpdateRequest request);
        Task DeleteAsync(int id);

        // Métodos para productos de la orden
        Task<IEnumerable<OrdenReabastecimientoProductoResponse>> GetProductosByOrdenAsync(int ordenId);
        Task<OrdenReabastecimientoProductoResponse?> GetProductoByIdsAsync(int ordenId, int productoId);
        Task<OrdenReabastecimientoProductoResponse> AddProductoAsync(OrdenReabastecimientoProductoCreateRequest request);
        Task UpdateProductoAsync(int ordenId, int productoId, OrdenReabastecimientoProductoUpdateRequest request);
        Task RemoveProductoAsync(int ordenId, int productoId);
        Task ActivateProductoAsync(int ordenId, int productoId);
        Task DeactivateProductoAsync(int ordenId, int productoId);
    }
}
