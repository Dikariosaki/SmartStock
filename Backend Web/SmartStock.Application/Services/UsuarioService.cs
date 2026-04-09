using SmartStock.Application.DTOs;
using SmartStock.Application.DTOs.Common;
using SmartStock.Application.Helpers;
using SmartStock.Application.Interfaces;
using SmartStock.Domain.Entities;
using System.Linq.Expressions;

namespace SmartStock.Application.Services
{
    public class UsuarioService : IUsuarioService
    {
        private readonly IUsuarioRepository _usuarioRepository;
        private readonly IClienteRepository _clienteRepository;
        private readonly IProveedorRepository _proveedorRepository;
        private readonly IRolRepository _rolRepository;
        private readonly ITareaRepository _tareaRepository;
        private readonly IUnitOfWork _uow;

        public UsuarioService(
            IUsuarioRepository usuarioRepository,
            IClienteRepository clienteRepository,
            IProveedorRepository proveedorRepository,
            IRolRepository rolRepository,
            ITareaRepository tareaRepository,
            IUnitOfWork uow)
        {
            _usuarioRepository = usuarioRepository;
            _clienteRepository = clienteRepository;
            _proveedorRepository = proveedorRepository;
            _rolRepository = rolRepository;
            _tareaRepository = tareaRepository;
            _uow = uow;
        }

        public async Task<IEnumerable<UsuarioResponse>> GetAllAsync()
        {
            var usuarios = await _usuarioRepository.ListAsync();
            return usuarios.Select(MapToResponse);
        }

        public async Task<PagedResponse<UsuarioResponse>> GetPagedAsync(PagedRequest request)
        {
            List<int>? roleIds = null;
            if (request.RoleNames != null && request.RoleNames.Any())
            {
                var roles = await _rolRepository.ListAsync();
                roleIds = roles
                    .Where(r => request.RoleNames.Contains(r.Nombre, StringComparer.OrdinalIgnoreCase))
                    .Select(r => r.RolId)
                    .ToList();
            }

            Expression<Func<Usuario, bool>> predicate = u =>
                (!request.Estado.HasValue || u.Estado == request.Estado.Value) &&
                (roleIds == null || roleIds.Contains(u.RolId));

            var (items, totalCount) = await _usuarioRepository.GetPagedAsync(
                request.PageNumber,
                request.PageSize,
                predicate,
                orderBy: q => q.OrderBy(u => u.Nombre));

            var dtos = items.Select(MapToResponse);
            return new PagedResponse<UsuarioResponse>(dtos, request.PageNumber, request.PageSize, totalCount);
        }

        public async Task<UsuarioResponse?> GetByIdAsync(int id)
        {
            var usuario = await _usuarioRepository.GetByIdAsync(id);
            return usuario == null ? null : MapToResponse(usuario);
        }

        public async Task<UsuarioResponse> CreateAsync(UsuarioCreateRequest request)
        {
            if (await _usuarioRepository.EmailExistsAsync(request.Email))
                throw new InvalidOperationException("El email ya está en uso");

            // Validar contraseña para roles internos (1: admin, 2: supervisor, 3: auxiliar)
            var isInternalRole = request.RolId >= 1 && request.RolId <= 3;
            if (isInternalRole && string.IsNullOrWhiteSpace(request.Password))
            {
                throw new InvalidOperationException("La contraseña es obligatoria para usuarios internos");
            }

            var usuario = new Usuario
            {
                RolId = request.RolId,
                Nombre = request.Nombre,
                Cedula = request.Cedula,
                Email = request.Email,
                PasswordHash = !string.IsNullOrWhiteSpace(request.Password)
                    ? Hashing.BcryptHash(request.Password)
                    : string.Empty,
                Telefono = request.Telefono ?? string.Empty,
                Estado = request.Estado
            };

            await _usuarioRepository.AddAsync(usuario);
            await _uow.CommitAsync();

            return MapToResponse(usuario);
        }

        public async Task UpdateAsync(int id, UsuarioUpdateRequest request)
        {
            var usuario = await _usuarioRepository.GetByIdAsync(id);
            if (usuario == null)
                throw new KeyNotFoundException($"Usuario con ID {id} no encontrado");

            if (!string.Equals(usuario.Email, request.Email, StringComparison.OrdinalIgnoreCase))
            {
                if (await _usuarioRepository.EmailExistsAsync(request.Email, excludeUsuarioId: id))
                    throw new InvalidOperationException("El email ya está en uso");
            }

            // Validar contraseña para roles internos si se está cambiando el rol a uno interno
            // o si ya era interno y no tiene contraseña (aunque esto último no debería pasar con la nueva lógica)
            var isInternalRole = request.RolId >= 1 && request.RolId <= 3;
            if (isInternalRole && string.IsNullOrWhiteSpace(usuario.PasswordHash) && string.IsNullOrWhiteSpace(request.Password))
            {
                throw new InvalidOperationException("La contraseña es obligatoria para usuarios internos");
            }

            usuario.RolId = request.RolId;
            usuario.Nombre = request.Nombre;
            usuario.Cedula = request.Cedula;
            usuario.Email = request.Email;
            usuario.Telefono = request.Telefono ?? string.Empty;
            usuario.Estado = request.Estado;

            if (!string.IsNullOrWhiteSpace(request.Password))
            {
                usuario.PasswordHash = Hashing.BcryptHash(request.Password);
            }

            await _usuarioRepository.UpdateAsync(usuario);
            await _uow.CommitAsync();
        }

        public async Task DeleteAsync(int id)
        {
            if (id == 1)
                throw new InvalidOperationException("El usuario con ID 1 no puede ser eliminado");

            var usuario = await _usuarioRepository.GetByIdAsync(id);
            if (usuario == null)
                throw new KeyNotFoundException($"Usuario con ID {id} no encontrado");

            // Verificar si tiene tareas asociadas (Hard delete prevention)
            var tareas = await _tareaRepository.ListAsync(t => t.AsignadoA == id);
            if (tareas.Any())
            {
                throw new InvalidOperationException("No se puede eliminar el usuario porque tiene tareas asociadas. Por favor, reasigne o elimine las tareas primero.");
            }

            // 1. Eliminar clientes relacionados
            var clientes = await _clienteRepository.GetByUsuarioAsync(id);
            foreach (var cliente in clientes)
            {
                await _clienteRepository.DeleteAsync(cliente);
            }

            // 2. Eliminar proveedores relacionados
            var proveedores = await _proveedorRepository.ListByUsuarioIdAsync(id);
            foreach (var proveedor in proveedores)
            {
                await _proveedorRepository.DeleteAsync(proveedor);
            }

            // 3. Eliminar usuario
            await _usuarioRepository.DeleteAsync(usuario);
            await _uow.CommitAsync();
        }

        public async Task ActivateAsync(int id)
        {
            var usuario = await _usuarioRepository.GetByIdAsync(id);
            if (usuario == null)
                throw new KeyNotFoundException($"Usuario con ID {id} no encontrado");

            usuario.Estado = true;
            await _usuarioRepository.UpdateAsync(usuario);
            await _uow.CommitAsync();
        }

        public async Task DeactivateAsync(int id)
        {
            var usuario = await _usuarioRepository.GetByIdAsync(id);
            if (usuario == null)
                throw new KeyNotFoundException($"Usuario con ID {id} no encontrado");

            usuario.Estado = false;
            await _usuarioRepository.UpdateAsync(usuario);
            await _uow.CommitAsync();
        }

        public async Task<bool> EmailExistsAsync(string email, int? excludeId = null)
        {
            if (excludeId.HasValue)
                return await _usuarioRepository.EmailExistsAsync(email, excludeId.Value);
            return await _usuarioRepository.EmailExistsAsync(email);
        }

        private static UsuarioResponse MapToResponse(Usuario u) => new UsuarioResponse
        {
            UsuarioId = u.UsuarioId,
            RolId = u.RolId,
            Nombre = u.Nombre,
            Cedula = u.Cedula,
            Email = u.Email,
            Telefono = u.Telefono ?? string.Empty,
            Estado = u.Estado
        };
    }
}
