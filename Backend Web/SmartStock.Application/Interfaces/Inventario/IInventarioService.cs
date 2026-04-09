using SmartStock.Application.DTOs;
using SmartStock.Application.DTOs.Common;

namespace SmartStock.Application.Interfaces
{
    public interface IInventarioService
    {
        Task<IEnumerable<InventarioResponse>> GetAllAsync();
        Task<PagedResponse<InventarioResponse>> GetPagedAsync(PagedRequest request);
        Task<IEnumerable<InventarioStockBajoResponse>> GetStockBajoMinimoAsync(int limit = 50);
        Task<InventarioResponse?> GetByIdAsync(int id);
        Task<IEnumerable<InventarioResponse>> GetByProductoAsync(int productoId);
        Task<InventarioResponse> CreateAsync(InventarioCreateRequest request);
        Task UpdateAsync(int id, InventarioUpdateRequest request);
        Task<int> RegistrarEntradaAsync(MovimientoStockRequest request);
        Task<int> RegistrarSalidaAsync(MovimientoStockRequest request);
        Task DeleteAsync(int id);
        Task ActivateAsync(int id);
        Task DeactivateAsync(int id);
    }
}
