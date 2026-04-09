using SmartStock.Application.DTOs;
using SmartStock.Application.DTOs.Common;
using SmartStock.Application.Interfaces;
using SmartStock.Domain.Entities;
using System.Linq.Expressions;

namespace SmartStock.Application.Services
{
    public class ProductoService : IProductoService
    {
        private readonly IProductoRepository _repository;
        private readonly IUnitOfWork _uow;

        public ProductoService(IProductoRepository repository, IUnitOfWork uow)
        {
            _repository = repository;
            _uow = uow;
        }

        public async Task<IEnumerable<ProductoResponse>> GetAllAsync()
        {
            var productos = await _repository.ListAsync();
            return productos.Select(MapToResponse);
        }

        public async Task<PagedResponse<ProductoResponse>> GetPagedAsync(PagedRequest request)
        {
            Expression<Func<Producto, bool>> predicate = p =>
                (!request.Estado.HasValue || p.Estado == request.Estado.Value);

            var (items, totalCount) = await _repository.GetPagedAsync(
                request.PageNumber,
                request.PageSize,
                predicate,
                orderBy: q => q.OrderBy(p => p.Nombre));

            var dtos = items.Select(MapToResponse);
            return new PagedResponse<ProductoResponse>(dtos, request.PageNumber, request.PageSize, totalCount);
        }

        public async Task<ProductoResponse?> GetByIdAsync(int id)
        {
            var producto = await _repository.GetByIdAsync(id);
            return producto == null ? null : MapToResponse(producto);
        }

        public async Task<ProductoResponse> CreateAsync(ProductoCreateRequest request)
        {
            // Auto-generar código único si no viene del frontend
            string codigo = request.Codigo ?? $"PROD-{DateTime.Now:yyyyMMddHHmmss}";

            if (await _repository.CodigoExistsAsync(codigo))
                throw new InvalidOperationException("El código de producto ya está en uso");

            var producto = new Producto
            {
                SubcategoriaId = request.SubcategoriaId,
                Codigo = codigo,
                Nombre = request.Nombre,
                Descripcion = request.Descripcion,
                PrecioUnitario = request.PrecioUnitario,
                Estado = request.Estado
            };

            await _repository.AddAsync(producto);
            await _uow.CommitAsync();

            return MapToResponse(producto);
        }

        public async Task UpdateAsync(int id, ProductoUpdateRequest request)
        {
            var producto = await _repository.GetByIdAsync(id);
            if (producto == null) throw new KeyNotFoundException($"Producto with id {id} not found.");

            if (!string.Equals(producto.Codigo, request.Codigo, StringComparison.OrdinalIgnoreCase))
            {
                if (await _repository.CodigoExistsAsync(request.Codigo, excludeProductoId: id))
                    throw new InvalidOperationException("El código de producto ya está en uso");
            }

            producto.SubcategoriaId = request.SubcategoriaId;
            producto.Codigo = request.Codigo;
            producto.Nombre = request.Nombre;
            producto.Descripcion = request.Descripcion;
            producto.PrecioUnitario = request.PrecioUnitario;
            producto.Estado = request.Estado;

            await _repository.UpdateAsync(producto);
            await _uow.CommitAsync();
        }

        public async Task DeactivateAsync(int id)
        {
            var producto = await _repository.GetByIdAsync(id);
            if (producto == null) throw new KeyNotFoundException($"Producto with id {id} not found.");

            producto.Estado = false;

            await _repository.UpdateAsync(producto);
            await _uow.CommitAsync();
        }

        public async Task ActivateAsync(int id)
        {
            var producto = await _repository.GetByIdAsync(id);
            if (producto == null) throw new KeyNotFoundException($"Producto with id {id} not found.");

            producto.Estado = true;

            await _repository.UpdateAsync(producto);
            await _uow.CommitAsync();
        }

        public async Task DeleteAsync(int id)
        {
            var producto = await _repository.GetByIdAsync(id);
            if (producto == null) throw new KeyNotFoundException($"Producto with id {id} not found.");

            await _repository.DeleteAsync(producto);
            await _uow.CommitAsync();
        }

        private static ProductoResponse MapToResponse(Producto producto)
        {
            return new ProductoResponse
            {
                ProductoId = producto.ProductoId,
                SubcategoriaId = producto.SubcategoriaId,
                Codigo = producto.Codigo,
                Nombre = producto.Nombre,
                Descripcion = producto.Descripcion,
                PrecioUnitario = producto.PrecioUnitario,
                Estado = producto.Estado
            };
        }
    }
}
