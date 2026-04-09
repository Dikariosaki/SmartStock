using SmartStock.Application.DTOs;
using SmartStock.Application.DTOs.Common;
using SmartStock.Application.Interfaces;
using SmartStock.Domain.Entities;
using System.Linq.Expressions;

namespace SmartStock.Application.Services
{
    public class RolService : IRolService
    {
        private readonly IRolRepository _rolRepository;
        private readonly IUnitOfWork _uow;

        public RolService(IRolRepository rolRepository, IUnitOfWork uow)
        {
            _rolRepository = rolRepository;
            _uow = uow;
        }

        public async Task<IEnumerable<RolResponse>> GetAllAsync()
        {
            var roles = await _rolRepository.ListAsync();
            return roles.Select(MapToResponse);
        }

        public async Task<PagedResponse<RolResponse>> GetPagedAsync(PagedRequest request)
        {
            Expression<Func<Rol, bool>>? predicate = null;

            if (!string.IsNullOrEmpty(request.Search))
            {
                predicate = r => r.Nombre.Contains(request.Search);
            }

            var (items, totalCount) = await _rolRepository.GetPagedAsync(
                request.PageNumber,
                request.PageSize,
                predicate,
                orderBy: q => q.OrderBy(r => r.Nombre));

            var dtos = items.Select(MapToResponse);
            return new PagedResponse<RolResponse>(dtos, request.PageNumber, request.PageSize, totalCount);
        }

        public async Task<RolResponse?> GetByIdAsync(int id)
        {
            var rol = await _rolRepository.GetByIdAsync(id);
            return rol == null ? null : MapToResponse(rol);
        }

        public async Task<RolResponse> CreateAsync(RolCreateRequest request)
        {
            if (await _rolRepository.NombreExisteAsync(request.Nombre))
                throw new InvalidOperationException("El nombre de rol ya está en uso");

            var rol = new Rol { Nombre = request.Nombre };
            await _rolRepository.AddAsync(rol);
            await _uow.CommitAsync();
            return MapToResponse(rol);
        }

        public async Task UpdateAsync(int id, RolUpdateRequest request)
        {
            var rol = await _rolRepository.GetByIdAsync(id);
            if (rol == null)
                throw new KeyNotFoundException($"Rol con ID {id} no encontrado");

            if (!string.Equals(rol.Nombre, request.Nombre, StringComparison.OrdinalIgnoreCase))
            {
                if (await _rolRepository.NombreExisteAsync(request.Nombre, excludeId: id))
                    throw new InvalidOperationException("El nombre de rol ya está en uso");
            }

            rol.Nombre = request.Nombre;
            await _rolRepository.UpdateAsync(rol);
            await _uow.CommitAsync();
        }

        public async Task DeleteAsync(int id)
        {
            var rol = await _rolRepository.GetByIdAsync(id);
            if (rol == null)
                throw new KeyNotFoundException($"Rol con ID {id} no encontrado");

            await _rolRepository.DeleteAsync(rol);
            await _uow.CommitAsync();
        }

        private static RolResponse MapToResponse(Rol r) => new RolResponse
        {
            RolId = r.RolId,
            Nombre = r.Nombre
        };
    }
}
