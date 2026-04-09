using System;
using System.Collections.Generic;
using System.Linq;
using System.Threading.Tasks;
using SmartStock.Application.DTOs;
using SmartStock.Application.DTOs.Common;
using SmartStock.Application.Interfaces;
using SmartStock.Domain.Entities;
using System.Linq.Expressions;

namespace SmartStock.Application.Services
{
    public class InventarioService : IInventarioService
    {
        private readonly IInventarioRepository _repository;
        private readonly IProductoRepository _productoRepository;
        private readonly IMovimientoRepository _movimientoRepository;
        private readonly IUnitOfWork _uow;

        public InventarioService(
            IInventarioRepository repository,
            IProductoRepository productoRepository,
            IMovimientoRepository movimientoRepository,
            IUnitOfWork uow)
        {
            _repository = repository;
            _productoRepository = productoRepository;
            _movimientoRepository = movimientoRepository;
            _uow = uow;
        }

        public async Task<IEnumerable<InventarioResponse>> GetAllAsync()
        {
            var inventarios = await _repository.ListAsync();
            return inventarios.Select(MapToResponse);
        }

        public async Task<PagedResponse<InventarioResponse>> GetPagedAsync(PagedRequest request)
        {
            Expression<Func<Inventario, bool>> predicate = i =>
                (!request.Estado.HasValue || i.Estado == request.Estado.Value);

            var (items, totalCount) = await _repository.GetPagedAsync(
                request.PageNumber,
                request.PageSize,
                predicate,
                orderBy: q => q.OrderByDescending(i => i.InventarioId)); // Or some other order

            var dtos = items.Select(MapToResponse);
            return new PagedResponse<InventarioResponse>(dtos, request.PageNumber, request.PageSize, totalCount);
        }

        public async Task<IEnumerable<InventarioStockBajoResponse>> GetStockBajoMinimoAsync(int limit = 50)
        {
            var safeLimit = Math.Clamp(limit, 1, 200);

            var inventariosBajoStock = await _repository.ListAsync(i => i.Estado && i.Cantidad < i.PuntoReorden);
            if (inventariosBajoStock.Count == 0)
            {
                return Enumerable.Empty<InventarioStockBajoResponse>();
            }

            var productoIds = inventariosBajoStock.Select(i => i.ProductoId).Distinct().ToList();
            var productos = await _productoRepository.ListAsync(p => productoIds.Contains(p.ProductoId));
            var productosPorId = productos.ToDictionary(p => p.ProductoId);

            return inventariosBajoStock
                .OrderBy(i => i.Cantidad - i.PuntoReorden)
                .ThenBy(i => i.InventarioId)
                .Take(safeLimit)
                .Select(i =>
                {
                    productosPorId.TryGetValue(i.ProductoId, out var producto);
                    return MapToStockBajoResponse(i, producto);
                })
                .ToList();
        }

        public async Task<InventarioResponse?> GetByIdAsync(int id)
        {
            var inventario = await _repository.GetByIdAsync(id);
            return inventario == null ? null : MapToResponse(inventario);
        }

        public async Task<IEnumerable<InventarioResponse>> GetByProductoAsync(int productoId)
        {
            var inventarios = await _repository.GetByProductoAsync(productoId);
            return inventarios.Select(MapToResponse);
        }

        public async Task<InventarioResponse> CreateAsync(InventarioCreateRequest request)
        {
            if (await _repository.ExisteProductoUbicacionAsync(request.ProductoId, request.Ubicacion))
                throw new InvalidOperationException("Ya existe inventario para ese producto en esa ubicación");

            var inventario = new Inventario
            {
                ProductoId = request.ProductoId,
                Ubicacion = request.Ubicacion,
                Cantidad = request.Cantidad,
                PuntoReorden = request.PuntoReorden,
                Estado = request.Estado
            };

            await _repository.AddAsync(inventario);
            await _uow.CommitAsync();

            return MapToResponse(inventario);
        }

        public async Task UpdateAsync(int id, InventarioUpdateRequest request)
        {
            var inventario = await _repository.GetByIdAsync(id);
            if (inventario == null) throw new KeyNotFoundException($"Inventario with id {id} not found.");

            if (inventario.ProductoId != request.ProductoId || !string.Equals(inventario.Ubicacion, request.Ubicacion, StringComparison.OrdinalIgnoreCase))
            {
                if (await _repository.ExisteProductoUbicacionAsync(request.ProductoId, request.Ubicacion, excludeId: id))
                    throw new InvalidOperationException("Ya existe inventario para ese producto en esa ubicación");
            }

            inventario.ProductoId = request.ProductoId;
            inventario.Ubicacion = request.Ubicacion;
            inventario.Cantidad = request.Cantidad;
            inventario.PuntoReorden = request.PuntoReorden;
            inventario.Estado = request.Estado;

            await _repository.UpdateAsync(inventario);
            await _uow.CommitAsync();
        }

        public async Task<int> RegistrarEntradaAsync(MovimientoStockRequest request)
        {
            var inventarios = await _repository.GetByProductoAsync(request.ProductoId);
            var inventario = inventarios.FirstOrDefault();

            if (inventario == null)
            {
                inventario = new Inventario
                {
                    ProductoId = request.ProductoId,
                    Ubicacion = "Principal",
                    Cantidad = request.Cantidad,
                    PuntoReorden = 10,
                    Estado = true
                };
                await _repository.AddAsync(inventario);
                await _uow.CommitAsync();
            }
            else
            {
                inventario.Cantidad += request.Cantidad;
                await _repository.UpdateAsync(inventario);
            }

            var movimiento = new Movimiento
            {
                InventarioId = inventario.InventarioId,
                UsuarioId = request.UsuarioId,
                ProveedorId = request.ProveedorId,
                Tipo = "Entrada",
                Cantidad = request.Cantidad,
                Lote = request.Lote,
                FechaMovimiento = DateTime.UtcNow.AddHours(-5), // Hora Colombia (UTC-5)
                Estado = true
            };
            await _movimientoRepository.AddAsync(movimiento);
            await _uow.CommitAsync();

            return inventario.Cantidad;
        }

        public async Task<int> RegistrarSalidaAsync(MovimientoStockRequest request)
        {
            var inventarios = await _repository.GetByProductoAsync(request.ProductoId);
            var inventario = inventarios.FirstOrDefault();

            if (inventario == null)
                throw new InvalidOperationException("No hay inventario disponible para este producto");

            if (inventario.Cantidad < request.Cantidad)
                throw new InvalidOperationException($"Stock insuficiente. Disponible: {inventario.Cantidad}");

            inventario.Cantidad -= request.Cantidad;
            await _repository.UpdateAsync(inventario);

            var movimiento = new Movimiento
            {
                InventarioId = inventario.InventarioId,
                UsuarioId = request.UsuarioId,
                Tipo = "Salida",
                Cantidad = request.Cantidad,
                ClienteId = request.ClienteId,
                Lote = request.Lote,
                FechaMovimiento = DateTime.UtcNow.AddHours(-5), // Hora Colombia (UTC-5)
                Estado = true
            };
            await _movimientoRepository.AddAsync(movimiento);
            await _uow.CommitAsync();

            return inventario.Cantidad;
        }

        public async Task DeleteAsync(int id)
        {
            var inventario = await _repository.GetByIdAsync(id);
            if (inventario == null) throw new KeyNotFoundException($"Inventario with id {id} not found.");
            await _repository.DeleteAsync(inventario);
            await _uow.CommitAsync();
        }

        public async Task ActivateAsync(int id)
        {
            var inventario = await _repository.GetByIdAsync(id);
            if (inventario == null) throw new KeyNotFoundException($"Inventario with id {id} not found.");
            inventario.Estado = true;
            await _repository.UpdateAsync(inventario);
            await _uow.CommitAsync();
        }

        public async Task DeactivateAsync(int id)
        {
            var inventario = await _repository.GetByIdAsync(id);
            if (inventario == null) throw new KeyNotFoundException($"Inventario with id {id} not found.");
            inventario.Estado = false;
            await _repository.UpdateAsync(inventario);
            await _uow.CommitAsync();
        }

        private static InventarioResponse MapToResponse(Inventario inventario)
        {
            return new InventarioResponse
            {
                InventarioId = inventario.InventarioId,
                ProductoId = inventario.ProductoId,
                Ubicacion = inventario.Ubicacion,
                Cantidad = inventario.Cantidad,
                PuntoReorden = inventario.PuntoReorden,
                Estado = inventario.Estado
            };
        }

        private static InventarioStockBajoResponse MapToStockBajoResponse(Inventario inventario, Producto? producto)
        {
            return new InventarioStockBajoResponse
            {
                InventarioId = inventario.InventarioId,
                ProductoId = inventario.ProductoId,
                Ubicacion = inventario.Ubicacion,
                Cantidad = inventario.Cantidad,
                PuntoReorden = inventario.PuntoReorden,
                Estado = inventario.Estado,
                ProductoNombre = producto?.Nombre ?? string.Empty,
                ProductoCodigo = producto?.Codigo ?? string.Empty
            };
        }
    }
}
