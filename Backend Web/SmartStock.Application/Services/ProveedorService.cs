using SmartStock.Application.DTOs;
using SmartStock.Application.DTOs.Common;
using SmartStock.Application.Interfaces;
using SmartStock.Domain.Entities;
using System.Linq.Expressions;

namespace SmartStock.Application.Services
{
    public class ProveedorService : IProveedorService
    {
        private readonly IProveedorRepository _repository;
        private readonly IUnitOfWork _uow;

        public ProveedorService(IProveedorRepository repository, IUnitOfWork uow)
        {
            _repository = repository;
            _uow = uow;
        }

        public async Task<IEnumerable<ProveedorResponse>> GetAllAsync()
        {
            var proveedores = await _repository.ListAsync();
            return proveedores.Select(MapToResponse);
        }

        public async Task<PagedResponse<ProveedorResponse>> GetPagedAsync(PagedRequest request)
        {
            Expression<Func<Proveedor, bool>> predicate = p =>
                (!request.Estado.HasValue || (p.Usuario != null && p.Usuario.Estado == request.Estado.Value));

            var (items, totalCount) = await _repository.GetPagedAsync(
                request.PageNumber,
                request.PageSize,
                predicate,
                orderBy: q => q.OrderBy(p => p.ProveedorId));

            var dtos = items.Select(MapToResponse);
            return new PagedResponse<ProveedorResponse>(dtos, request.PageNumber, request.PageSize, totalCount);
        }

        public async Task<ProveedorResponse?> GetByIdAsync(int id)
        {
            var proveedor = await _repository.GetByIdAsync(id);
            return proveedor == null ? null : MapToResponse(proveedor);
        }

        public async Task<IEnumerable<ProveedorResponse>> GetByUsuarioIdAsync(int usuarioId)
        {
            var proveedores = await _repository.ListByUsuarioIdAsync(usuarioId);
            return proveedores.Select(MapToResponse);
        }

        public async Task<ProveedorResponse> CreateAsync(ProveedorCreateRequest request)
        {
            var proveedor = new Proveedor
            {
                UsuarioId = request.UsuarioId,
                Contacto = request.Contacto
            };

            await _repository.AddAsync(proveedor);
            await _uow.CommitAsync();

            return MapToResponse(proveedor);
        }

        public async Task UpdateAsync(int id, ProveedorUpdateRequest request)
        {
            var proveedor = await _repository.GetByIdAsync(id);
            if (proveedor == null) throw new KeyNotFoundException($"Proveedor with id {id} not found.");

            if (request.UsuarioId.HasValue)
                proveedor.UsuarioId = request.UsuarioId;
            if (request.Contacto != null)
                proveedor.Contacto = request.Contacto;

            await _repository.UpdateAsync(proveedor);
            await _uow.CommitAsync();
        }

        public async Task DeleteAsync(int id)
        {
            var proveedor = await _repository.GetByIdAsync(id);
            if (proveedor == null) throw new KeyNotFoundException($"Proveedor with id {id} not found.");

            await _repository.DeleteAsync(proveedor);
            await _uow.CommitAsync();
        }

        private static ProveedorResponse MapToResponse(Proveedor p) => new ProveedorResponse
        {
            ProveedorId = p.ProveedorId,
            UsuarioId = p.UsuarioId,
            Contacto = p.Contacto,
            Usuario = p.Usuario != null ? new UsuarioResponse
            {
                UsuarioId = p.Usuario.UsuarioId,
                RolId = p.Usuario.RolId,
                Nombre = p.Usuario.Nombre,
                Cedula = p.Usuario.Cedula,
                Email = p.Usuario.Email,
                Telefono = p.Usuario.Telefono ?? string.Empty,
                Estado = p.Usuario.Estado
            } : null
        };
    }
}
