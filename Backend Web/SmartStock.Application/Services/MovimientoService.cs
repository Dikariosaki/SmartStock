using SmartStock.Application.DTOs;
using SmartStock.Application.DTOs.Common;
using SmartStock.Application.Interfaces;
using SmartStock.Domain.Entities;
using System.Linq.Expressions;

namespace SmartStock.Application.Services
{
    public class MovimientoService : IMovimientoService
    {
        private readonly IMovimientoRepository _movimientoRepository;
        private readonly IInventarioRepository _inventarioRepository;
        private readonly IProductoRepository _productoRepository;
        private readonly IProveedorRepository _proveedorRepository;
        private readonly IClienteRepository _clienteRepository;
        private readonly IUsuarioRepository _usuarioRepository;
        private readonly IUnitOfWork _uow;

        public MovimientoService(
            IMovimientoRepository movimientoRepository,
            IInventarioRepository inventarioRepository,
            IProductoRepository productoRepository,
            IProveedorRepository proveedorRepository,
            IClienteRepository clienteRepository,
            IUsuarioRepository usuarioRepository,
            IUnitOfWork uow)
        {
            _movimientoRepository = movimientoRepository;
            _inventarioRepository = inventarioRepository;
            _productoRepository = productoRepository;
            _proveedorRepository = proveedorRepository;
            _clienteRepository = clienteRepository;
            _usuarioRepository = usuarioRepository;
            _uow = uow;
        }

        public async Task<IEnumerable<MovimientoResponse>> GetAllAsync()
        {
            var movimientos = await _movimientoRepository.ListAsync();
            var responses = new List<MovimientoResponse>();
            foreach (var mov in movimientos)
            {
                responses.Add(await MapToResponseAsync(mov));
            }
            return responses;
        }

        public async Task<PagedResponse<MovimientoResponse>> GetPagedAsync(PagedRequest request)
        {
            Expression<Func<Movimiento, bool>> predicate = m =>
                (!request.Estado.HasValue || m.Estado == request.Estado.Value) &&
                (string.IsNullOrEmpty(request.Tipo) || m.Tipo == request.Tipo);

            var (items, totalCount) = await _movimientoRepository.GetPagedAsync(
                request.PageNumber,
                request.PageSize,
                predicate,
                orderBy: q => q.OrderByDescending(m => m.FechaMovimiento));

            var responses = new List<MovimientoResponse>();
            foreach (var item in items)
            {
                responses.Add(await MapToResponseAsync(item));
            }

            return new PagedResponse<MovimientoResponse>(responses, request.PageNumber, request.PageSize, totalCount);
        }

        public async Task<MovimientoResponse?> GetByIdAsync(int id)
        {
            var mov = await _movimientoRepository.GetByIdAsync(id);
            return mov == null ? null : await MapToResponseAsync(mov);
        }

        public async Task<IEnumerable<MovimientoResponse>> GetByInventarioAsync(int inventarioId)
        {
            var movimientos = await _movimientoRepository.ListAsync(m => m.InventarioId == inventarioId);
            var responses = new List<MovimientoResponse>();
            foreach (var mov in movimientos)
            {
                responses.Add(await MapToResponseAsync(mov));
            }
            return responses;
        }

        public async Task<MovimientoResponse> CreateAsync(MovimientoCreateRequest request)
        {
            var mov = new Movimiento
            {
                InventarioId = request.InventarioId,
                OrdenId = request.OrdenId,
                UsuarioId = request.UsuarioId,
                ProveedorId = request.ProveedorId,
                ClienteId = request.ClienteId,
                Tipo = request.Tipo,
                Cantidad = request.Cantidad,
                FechaMovimiento = DateTime.UtcNow.AddHours(-5),
                Lote = request.Lote,
                Estado = request.Estado
            };

            await _movimientoRepository.AddAsync(mov);
            await _uow.CommitAsync();
            return await MapToResponseAsync(mov);
        }

        public async Task UpdateAsync(int id, MovimientoUpdateRequest request)
        {
            var mov = await _movimientoRepository.GetByIdAsync(id);
            if (mov == null)
                throw new KeyNotFoundException($"Movimiento con ID {id} no encontrado");

            mov.InventarioId = request.InventarioId;
            mov.OrdenId = request.OrdenId;
            mov.UsuarioId = request.UsuarioId;
            mov.ProveedorId = request.ProveedorId;
            mov.ClienteId = request.ClienteId;
            mov.Tipo = request.Tipo;
            mov.Cantidad = request.Cantidad;
            mov.Lote = request.Lote;
            mov.Estado = request.Estado;

            await _movimientoRepository.UpdateAsync(mov);
            await _uow.CommitAsync();
        }

        public async Task DeleteAsync(int id)
        {
            var mov = await _movimientoRepository.GetByIdAsync(id);
            if (mov == null)
                throw new KeyNotFoundException($"Movimiento con ID {id} no encontrado");

            await _movimientoRepository.DeleteAsync(mov);
            await _uow.CommitAsync();
        }

        public async Task ActivateAsync(int id)
        {
            var mov = await _movimientoRepository.GetByIdAsync(id);
            if (mov == null)
                throw new KeyNotFoundException($"Movimiento con ID {id} no encontrado");

            mov.Estado = true;
            await _movimientoRepository.UpdateAsync(mov);
            await _uow.CommitAsync();
        }

        public async Task DeactivateAsync(int id)
        {
            var mov = await _movimientoRepository.GetByIdAsync(id);
            if (mov == null)
                throw new KeyNotFoundException($"Movimiento con ID {id} no encontrado");

            mov.Estado = false;
            await _movimientoRepository.UpdateAsync(mov);
            await _uow.CommitAsync();
        }

        private async Task<MovimientoResponse> MapToResponseAsync(Movimiento m)
        {
            string? productoNombre = null;
            string? productoCodigo = null;
            string? proveedorNombre = null;
            string? clienteNombre = null;
            string? usuarioNombre = null;

            var usuario = await _usuarioRepository.GetByIdAsync(m.UsuarioId);
            if (usuario != null)
            {
                // Prioridad: Nombre -> Email -> "Usuario #ID"
                usuarioNombre = !string.IsNullOrEmpty(usuario.Nombre) ? usuario.Nombre : usuario.Email;
                if (string.IsNullOrEmpty(usuarioNombre))
                {
                    usuarioNombre = $"Usuario #{usuario.UsuarioId}";
                }
            }
            else
            {
                usuarioNombre = $"Usuario #{m.UsuarioId}";
            }

            // Obtener producto a través de inventario
            var inventario = await _inventarioRepository.GetByIdAsync(m.InventarioId);
            if (inventario != null)
            {
                var producto = await _productoRepository.GetByIdAsync(inventario.ProductoId);
                if (producto != null)
                {
                    productoNombre = producto.Nombre;
                    productoCodigo = producto.Codigo;
                }
            }
            else
            {
                productoNombre = $"Producto #{m.InventarioId}";
            }

            // Obtener proveedor si existe
            if (m.ProveedorId.HasValue)
            {
                var proveedor = await _proveedorRepository.GetByIdAsync(m.ProveedorId.Value);
                if (proveedor != null && proveedor.Usuario != null)
                {
                    proveedorNombre = !string.IsNullOrEmpty(proveedor.Usuario.Nombre) ? proveedor.Usuario.Nombre : proveedor.Usuario.Email;
                    if (string.IsNullOrEmpty(proveedorNombre))
                    {
                        proveedorNombre = $"Proveedor #{proveedor.ProveedorId}";
                    }
                }
                else
                {
                    proveedorNombre = $"Proveedor #{m.ProveedorId}";
                }
            }

            // Obtener cliente si existe
            if (m.ClienteId.HasValue)
            {
                var cliente = await _clienteRepository.GetByIdAsync(m.ClienteId.Value);
                if (cliente != null && cliente.Usuario != null)
                {
                    clienteNombre = !string.IsNullOrEmpty(cliente.Usuario.Nombre) ? cliente.Usuario.Nombre : cliente.Usuario.Email;
                    if (string.IsNullOrEmpty(clienteNombre))
                    {
                        clienteNombre = $"Cliente #{cliente.ClienteId}";
                    }
                }
                else
                {
                    clienteNombre = $"Cliente #{m.ClienteId}";
                }
            }

            return new MovimientoResponse
            {
                MovimientoId = m.MovimientoId,
                InventarioId = m.InventarioId,
                OrdenId = m.OrdenId,
                UsuarioId = m.UsuarioId,
                ProveedorId = m.ProveedorId,
                ClienteId = m.ClienteId,
                Tipo = m.Tipo,
                Cantidad = m.Cantidad,
                FechaMovimiento = m.FechaMovimiento,
                Lote = m.Lote,
                Estado = m.Estado,
                ProductoNombre = productoNombre,
                ProductoCodigo = productoCodigo,
                UsuarioNombre = usuarioNombre,
                ProveedorNombre = proveedorNombre,
                ClienteNombre = clienteNombre
            };
        }
    }
}
