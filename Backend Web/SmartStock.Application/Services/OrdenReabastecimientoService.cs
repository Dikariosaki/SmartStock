using SmartStock.Application.DTOs;
using SmartStock.Application.DTOs.Common;
using SmartStock.Application.Interfaces;
using SmartStock.Domain.Entities;
using System.Linq.Expressions;

namespace SmartStock.Application.Services
{
    public class OrdenReabastecimientoService : IOrdenReabastecimientoService
    {
        private readonly IOrdenReabastecimientoRepository _ordenRepository;
        private readonly IOrdenReabastecimientoProductoRepository _ordenProductoRepository;
        private readonly IUnitOfWork _uow;

        public OrdenReabastecimientoService(
            IOrdenReabastecimientoRepository ordenRepository,
            IOrdenReabastecimientoProductoRepository ordenProductoRepository,
            IUnitOfWork uow)
        {
            _ordenRepository = ordenRepository;
            _ordenProductoRepository = ordenProductoRepository;
            _uow = uow;
        }

        // Ordenes
        public async Task<IEnumerable<OrdenReabastecimientoResponse>> GetAllAsync()
        {
            var ordenes = await _ordenRepository.ListAsync();
            return ordenes.Select(MapToResponse);
        }

        public async Task<PagedResponse<OrdenReabastecimientoResponse>> GetPagedAsync(PagedRequest request)
        {
            // Note: OrdenReabastecimiento has string Estado, so we ignore the boolean filter for now
            // or we could map it if we knew the values.
            Expression<Func<OrdenReabastecimiento, bool>>? predicate = null;

            var (items, totalCount) = await _ordenRepository.GetPagedAsync(
                request.PageNumber,
                request.PageSize,
                predicate,
                orderBy: q => q.OrderByDescending(o => o.FechaCreacion));

            var dtos = items.Select(MapToResponse);
            return new PagedResponse<OrdenReabastecimientoResponse>(dtos, request.PageNumber, request.PageSize, totalCount);
        }

        public async Task<OrdenReabastecimientoResponse?> GetByIdAsync(int id)
        {
            var orden = await _ordenRepository.GetByIdAsync(id);
            return orden == null ? null : MapToResponse(orden);
        }

        public async Task<IEnumerable<OrdenReabastecimientoResponse>> GetByProveedorAsync(int proveedorId)
        {
            var ordenes = await _ordenRepository.GetByProveedorAsync(proveedorId);
            return ordenes.Select(MapToResponse);
        }

        public async Task<OrdenReabastecimientoResponse> CreateAsync(OrdenReabastecimientoCreateRequest request)
        {
            var orden = new OrdenReabastecimiento
            {
                ProveedorId = request.ProveedorId,
                FechaCreacion = DateTime.UtcNow,
                Estado = request.Estado
            };

            await _ordenRepository.AddAsync(orden);
            await _uow.CommitAsync();
            return MapToResponse(orden);
        }

        public async Task UpdateAsync(int id, OrdenReabastecimientoUpdateRequest request)
        {
            var orden = await _ordenRepository.GetByIdAsync(id);
            if (orden == null)
                throw new KeyNotFoundException($"Orden con ID {id} no encontrada");

            orden.ProveedorId = request.ProveedorId;
            orden.Estado = request.Estado;

            await _ordenRepository.UpdateAsync(orden);
            await _uow.CommitAsync();
        }

        public async Task DeleteAsync(int id)
        {
            var orden = await _ordenRepository.GetByIdAsync(id);
            if (orden == null)
                throw new KeyNotFoundException($"Orden con ID {id} no encontrada");

            await _ordenRepository.DeleteAsync(orden);
            await _uow.CommitAsync();
        }

        // Productos de la orden
        public async Task<IEnumerable<OrdenReabastecimientoProductoResponse>> GetProductosByOrdenAsync(int ordenId)
        {
            var items = await _ordenProductoRepository.ListByOrdenAsync(ordenId);
            return items.Select(MapToResponse);
        }

        public async Task<OrdenReabastecimientoProductoResponse?> GetProductoByIdsAsync(int ordenId, int productoId)
        {
            var item = await _ordenProductoRepository.GetByIdsAsync(ordenId, productoId);
            return item == null ? null : MapToResponse(item);
        }

        public async Task<OrdenReabastecimientoProductoResponse> AddProductoAsync(OrdenReabastecimientoProductoCreateRequest request)
        {
            var item = new OrdenReabastecimientoProducto
            {
                OrdenId = request.OrdenId,
                ProductoId = request.ProductoId,
                CantidadPedida = request.CantidadPedida,
                PrecioCompraUnitario = request.PrecioCompraUnitario,
                Estado = request.Estado
            };

            await _ordenProductoRepository.AddAsync(item);
            await _uow.CommitAsync();
            return MapToResponse(item);
        }

        public async Task UpdateProductoAsync(int ordenId, int productoId, OrdenReabastecimientoProductoUpdateRequest request)
        {
            var item = await _ordenProductoRepository.GetByIdsAsync(ordenId, productoId);
            if (item == null)
                throw new KeyNotFoundException($"Producto {productoId} en orden {ordenId} no encontrado");

            item.CantidadPedida = request.CantidadPedida;
            item.PrecioCompraUnitario = request.PrecioCompraUnitario;
            item.Estado = request.Estado;

            await _ordenProductoRepository.UpdateAsync(item);
            await _uow.CommitAsync();
        }

        public async Task RemoveProductoAsync(int ordenId, int productoId)
        {
            var item = await _ordenProductoRepository.GetByIdsAsync(ordenId, productoId);
            if (item == null)
                throw new KeyNotFoundException($"Producto {productoId} en orden {ordenId} no encontrado");

            await _ordenProductoRepository.DeleteAsync(item);
            await _uow.CommitAsync();
        }

        public async Task ActivateProductoAsync(int ordenId, int productoId)
        {
            var item = await _ordenProductoRepository.GetByIdsAsync(ordenId, productoId);
            if (item == null)
                throw new KeyNotFoundException($"Producto {productoId} en orden {ordenId} no encontrado");

            item.Estado = true;
            await _ordenProductoRepository.UpdateAsync(item);
            await _uow.CommitAsync();
        }

        public async Task DeactivateProductoAsync(int ordenId, int productoId)
        {
            var item = await _ordenProductoRepository.GetByIdsAsync(ordenId, productoId);
            if (item == null)
                throw new KeyNotFoundException($"Producto {productoId} en orden {ordenId} no encontrado");

            item.Estado = false;
            await _ordenProductoRepository.UpdateAsync(item);
            await _uow.CommitAsync();
        }

        private static OrdenReabastecimientoResponse MapToResponse(OrdenReabastecimiento o) => new OrdenReabastecimientoResponse
        {
            OrdenId = o.OrdenId,
            ProveedorId = o.ProveedorId,
            FechaCreacion = o.FechaCreacion,
            Estado = o.Estado
        };

        private static OrdenReabastecimientoProductoResponse MapToResponse(OrdenReabastecimientoProducto op) => new OrdenReabastecimientoProductoResponse
        {
            OrdenId = op.OrdenId,
            ProductoId = op.ProductoId,
            CantidadPedida = op.CantidadPedida,
            PrecioCompraUnitario = op.PrecioCompraUnitario,
            Estado = op.Estado
        };
    }
}
