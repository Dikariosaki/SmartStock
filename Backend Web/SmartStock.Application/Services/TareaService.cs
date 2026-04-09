using SmartStock.Application.DTOs;
using SmartStock.Application.DTOs.Common;
using SmartStock.Application.Interfaces;
using SmartStock.Domain.Entities;
using System.Linq.Expressions;

namespace SmartStock.Application.Services
{
    public class TareaService : ITareaService
    {
        private readonly ITareaRepository _tareaRepository;
        private readonly ITareaProductoRepository _tareaProductoRepository;
        private readonly IUsuarioRepository _usuarioRepository;
        private readonly IUnitOfWork _uow;

        public TareaService(
            ITareaRepository tareaRepository,
            ITareaProductoRepository tareaProductoRepository,
            IUsuarioRepository usuarioRepository,
            IUnitOfWork uow)
        {
            _tareaRepository = tareaRepository;
            _tareaProductoRepository = tareaProductoRepository;
            _usuarioRepository = usuarioRepository;
            _uow = uow;
        }

        // Tareas
        public async Task<IEnumerable<TareaResponse>> GetAllAsync()
        {
            var tareas = await _tareaRepository.ListAsync();
            var usuarios = await _usuarioRepository.ListAsync();
            var usuariosDict = usuarios.ToDictionary(u => u.UsuarioId, u => u.Nombre);

            return tareas.Select(t => MapToResponse(t, t.AsignadoA.HasValue && usuariosDict.ContainsKey(t.AsignadoA.Value) ? usuariosDict[t.AsignadoA.Value] : null));
        }

        public async Task<PagedResponse<TareaResponse>> GetPagedAsync(PagedRequest request)
        {
            Expression<Func<Tarea, bool>> predicate = t =>
                (!request.Estado.HasValue || t.Estado == request.Estado.Value);

            var (items, totalCount) = await _tareaRepository.GetPagedAsync(
                request.PageNumber,
                request.PageSize,
                predicate,
                orderBy: q => q.OrderByDescending(t => t.FechaCreacion));

            var usuarios = await _usuarioRepository.ListAsync();
            var usuariosDict = usuarios.ToDictionary(u => u.UsuarioId, u => u.Nombre);

            var dtos = items.Select(t => MapToResponse(t, t.AsignadoA.HasValue && usuariosDict.ContainsKey(t.AsignadoA.Value) ? usuariosDict[t.AsignadoA.Value] : null));
            return new PagedResponse<TareaResponse>(dtos, request.PageNumber, request.PageSize, totalCount);
        }

        public async Task<TareaResponse?> GetByIdAsync(int id)
        {
            var tarea = await _tareaRepository.GetByIdAsync(id);
            if (tarea == null) return null;

            string? usuarioNombre = null;
            if (tarea.AsignadoA.HasValue)
            {
                var usuario = await _usuarioRepository.GetByIdAsync(tarea.AsignadoA.Value);
                usuarioNombre = usuario?.Nombre;
            }

            return MapToResponse(tarea, usuarioNombre);
        }

        public async Task<TareaResponse> CreateAsync(TareaCreateRequest request)
        {
            var tarea = new Tarea
            {
                Titulo = request.Titulo,
                Descripcion = request.Descripcion,
                AsignadoA = request.AsignadoA,
                FechaCreacion = DateTime.UtcNow,
                Estado = request.Estado
            };

            await _tareaRepository.AddAsync(tarea);
            await _uow.CommitAsync();

            string? usuarioNombre = null;
            if (tarea.AsignadoA.HasValue)
            {
                var usuario = await _usuarioRepository.GetByIdAsync(tarea.AsignadoA.Value);
                usuarioNombre = usuario?.Nombre;
            }

            return MapToResponse(tarea, usuarioNombre);
        }

        public async Task UpdateAsync(int id, TareaUpdateRequest request)
        {
            var tarea = await _tareaRepository.GetByIdAsync(id);
            if (tarea == null)
                throw new KeyNotFoundException($"Tarea con ID {id} no encontrada");

            tarea.Titulo = request.Titulo;
            tarea.Descripcion = request.Descripcion;
            tarea.AsignadoA = request.AsignadoA;
            tarea.FechaFin = request.FechaFin;

            // Asegurar que el estado se actualice correctamente
            tarea.Estado = request.Estado;

            await _tareaRepository.UpdateAsync(tarea);
            await _uow.CommitAsync();
        }

        public async Task DeleteAsync(int id)
        {
            var tarea = await _tareaRepository.GetByIdAsync(id);
            if (tarea == null)
                throw new KeyNotFoundException($"Tarea con ID {id} no encontrada");

            await _tareaRepository.DeleteAsync(tarea);
            await _uow.CommitAsync();
        }

        public async Task ActivateAsync(int id)
        {
            var tarea = await _tareaRepository.GetByIdAsync(id);
            if (tarea == null)
                throw new KeyNotFoundException($"Tarea con ID {id} no encontrada");

            tarea.Estado = true;
            await _tareaRepository.UpdateAsync(tarea);
            await _uow.CommitAsync();
        }

        public async Task DeactivateAsync(int id)
        {
            var tarea = await _tareaRepository.GetByIdAsync(id);
            if (tarea == null)
                throw new KeyNotFoundException($"Tarea con ID {id} no encontrada");

            tarea.Estado = false;
            await _tareaRepository.UpdateAsync(tarea);
            await _uow.CommitAsync();
        }

        // TareaProductos
        public async Task<IEnumerable<TareaProductoResponse>> GetProductosByTareaAsync(int tareaId)
        {
            var items = await _tareaProductoRepository.ListByTareaAsync(tareaId);
            return items.Select(MapToResponse);
        }

        public async Task<IEnumerable<TareaProductoResponse>> GetTareasByProductoAsync(int productoId)
        {
            var items = await _tareaProductoRepository.ListByProductoAsync(productoId);
            return items.Select(MapToResponse);
        }

        public async Task<TareaProductoResponse?> GetProductoByIdsAsync(int tareaId, int productoId)
        {
            var item = await _tareaProductoRepository.GetByIdsAsync(tareaId, productoId);
            return item == null ? null : MapToResponse(item);
        }

        public async Task<TareaProductoResponse> AddProductoAsync(TareaProductoCreateRequest request)
        {
            var exists = await _tareaProductoRepository.GetByIdsAsync(request.TareaId, request.ProductoId);
            if (exists != null)
                throw new InvalidOperationException("El producto ya está asignado a esta tarea");

            var item = new TareaProducto
            {
                TareaId = request.TareaId,
                ProductoId = request.ProductoId,
                Cantidad = request.Cantidad,
                Estado = request.Estado
            };

            await _tareaProductoRepository.AddAsync(item);
            await _uow.CommitAsync();

            return MapToResponse(item);
        }

        public async Task UpdateProductoAsync(int tareaId, int productoId, TareaProductoUpdateRequest request)
        {
            var item = await _tareaProductoRepository.GetByIdsAsync(tareaId, productoId);
            if (item == null)
                throw new KeyNotFoundException($"Relación Tarea-Producto no encontrada");

            item.Cantidad = request.Cantidad;
            item.Estado = request.Estado;

            await _tareaProductoRepository.UpdateAsync(item);
            await _uow.CommitAsync();
        }

        public async Task RemoveProductoAsync(int tareaId, int productoId)
        {
            var item = await _tareaProductoRepository.GetByIdsAsync(tareaId, productoId);
            if (item == null)
                throw new KeyNotFoundException($"Relación Tarea-Producto no encontrada");

            await _tareaProductoRepository.DeleteAsync(item);
            await _uow.CommitAsync();
        }

        private static TareaResponse MapToResponse(Tarea tarea, string? usuarioNombre)
        {
            return new TareaResponse
            {
                TareaId = tarea.TareaId,
                Titulo = tarea.Titulo,
                Descripcion = tarea.Descripcion,
                AsignadoA = tarea.AsignadoA,
                UsuarioNombre = usuarioNombre,
                FechaCreacion = tarea.FechaCreacion,
                FechaFin = tarea.FechaFin,
                Estado = tarea.Estado
            };
        }

        private static TareaProductoResponse MapToResponse(TareaProducto item)
        {
            return new TareaProductoResponse
            {
                TareaId = item.TareaId,
                ProductoId = item.ProductoId,
                Cantidad = item.Cantidad,
                Estado = item.Estado
            };
        }
    }
}
