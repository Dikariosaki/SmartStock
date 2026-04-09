using SmartStock.Application.DTOs;
using SmartStock.Application.DTOs.Common;
using SmartStock.Application.Interfaces;
using SmartStock.Domain.Entities;
using System.Linq.Expressions;

namespace SmartStock.Application.Services
{
    public class ClienteService : IClienteService
    {
        private readonly IClienteRepository _repository;
        private readonly IUnitOfWork _uow;

        public ClienteService(IClienteRepository repository, IUnitOfWork uow)
        {
            _repository = repository;
            _uow = uow;
        }

        public async Task<IEnumerable<ClienteResponse>> GetAllAsync()
        {
            var clientes = await _repository.ListAsync();
            return clientes.Select(MapToResponse);
        }

        public async Task<PagedResponse<ClienteResponse>> GetPagedAsync(PagedRequest request)
        {
            Expression<Func<Cliente, bool>> predicate = c =>
                (!request.Estado.HasValue || (c.Usuario != null && c.Usuario.Estado == request.Estado.Value));

            var (items, totalCount) = await _repository.GetPagedAsync(
                request.PageNumber,
                request.PageSize,
                predicate,
                orderBy: q => q.OrderBy(c => c.ClienteId));

            var dtos = items.Select(MapToResponse);
            return new PagedResponse<ClienteResponse>(dtos, request.PageNumber, request.PageSize, totalCount);
        }

        public async Task<ClienteResponse?> GetByIdAsync(int id)
        {
            var cliente = await _repository.GetByIdAsync(id);
            return cliente == null ? null : MapToResponse(cliente);
        }

        public async Task<IEnumerable<ClienteResponse>> GetByUsuarioIdAsync(int usuarioId)
        {
            var clientes = await _repository.GetByUsuarioAsync(usuarioId);
            return clientes.Select(MapToResponse);
        }

        public async Task<ClienteResponse> CreateAsync(ClienteCreateRequest request)
        {
            var cliente = new Cliente
            {
                UsuarioId = request.UsuarioId,
                Contacto = request.Contacto,
                Direccion = request.Direccion,
                Sucursal = request.Sucursal
            };

            await _repository.AddAsync(cliente);
            await _uow.CommitAsync();

            // Reload to get potential navigation properties or db generated values if needed
            // For now, map the created entity
            return MapToResponse(cliente);
        }

        public async Task UpdateAsync(int id, ClienteUpdateRequest request)
        {
            var cliente = await _repository.GetByIdAsync(id);
            if (cliente == null) throw new KeyNotFoundException($"Cliente with id {id} not found.");

            if (request.UsuarioId.HasValue)
                cliente.UsuarioId = request.UsuarioId;
            if (request.Contacto != null)
                cliente.Contacto = request.Contacto;
            if (request.Direccion != null)
                cliente.Direccion = request.Direccion;
            if (request.Sucursal != null)
                cliente.Sucursal = request.Sucursal;

            if (request.Estado.HasValue && cliente.Usuario != null)
            {
                cliente.Usuario.Estado = request.Estado.Value;
            }

            await _repository.UpdateAsync(cliente);
            await _uow.CommitAsync();
        }

        public async Task DeleteAsync(int id)
        {
            var cliente = await _repository.GetByIdAsync(id);
            if (cliente == null) throw new KeyNotFoundException($"Cliente with id {id} not found.");

            await _repository.DeleteAsync(cliente);
            await _uow.CommitAsync();
        }

        private static ClienteResponse MapToResponse(Cliente c) => new ClienteResponse
        {
            ClienteId = c.ClienteId,
            UsuarioId = c.UsuarioId,
            Contacto = c.Contacto,
            Direccion = c.Direccion,
            Sucursal = c.Sucursal,
            Usuario = c.Usuario != null ? new UsuarioResponse
            {
                UsuarioId = c.Usuario.UsuarioId,
                RolId = c.Usuario.RolId,
                Nombre = c.Usuario.Nombre,
                Cedula = c.Usuario.Cedula,
                Email = c.Usuario.Email,
                Telefono = c.Usuario.Telefono ?? string.Empty,
                Estado = c.Usuario.Estado
            } : null
        };
    }
}
