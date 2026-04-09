using SmartStock.Application.DTOs;
using SmartStock.Application.DTOs.Common;
using SmartStock.Application.Interfaces;
using SmartStock.Domain.Entities;
using System.Linq.Expressions;

namespace SmartStock.Application.Services
{
    public class CategoriaService : ICategoriaService
    {
        private readonly ICategoriaRepository _repository;
        private readonly IUnitOfWork _uow;

        public CategoriaService(ICategoriaRepository repository, IUnitOfWork uow)
        {
            _repository = repository;
            _uow = uow;
        }

        public async Task<IEnumerable<CategoriaResponse>> GetAllAsync()
        {
            var categorias = await _repository.ListAsync();
            return categorias.Select(MapToResponse);
        }

        public async Task<PagedResponse<CategoriaResponse>> GetPagedAsync(PagedRequest request)
        {
            Expression<Func<Categoria, bool>> predicate = c =>
                (!request.Estado.HasValue || c.Estado == request.Estado.Value);

            var (items, totalCount) = await _repository.GetPagedAsync(
                request.PageNumber,
                request.PageSize,
                predicate,
                orderBy: q => q.OrderBy(c => c.Nombre));

            var dtos = items.Select(MapToResponse);
            return new PagedResponse<CategoriaResponse>(dtos, request.PageNumber, request.PageSize, totalCount);
        }

        public async Task<CategoriaResponse?> GetByIdAsync(int id)
        {
            var categoria = await _repository.GetByIdAsync(id);
            return categoria == null ? null : MapToResponse(categoria);
        }

        public async Task<CategoriaResponse> CreateAsync(CategoriaCreateRequest request)
        {
            if (await _repository.NombreExistsAsync(request.Nombre))
                throw new InvalidOperationException("El nombre de categoría ya está en uso");

            var categoria = new Categoria
            {
                Nombre = request.Nombre,
                Estado = request.Estado
            };

            await _repository.AddAsync(categoria);
            await _uow.CommitAsync();

            return MapToResponse(categoria);
        }

        public async Task UpdateAsync(int id, CategoriaUpdateRequest request)
        {
            var categoria = await _repository.GetByIdAsync(id);
            if (categoria == null) throw new KeyNotFoundException($"Categoria with id {id} not found.");

            if (!string.Equals(categoria.Nombre, request.Nombre, StringComparison.OrdinalIgnoreCase))
            {
                if (await _repository.NombreExistsAsync(request.Nombre, excludeCategoriaId: id))
                    throw new InvalidOperationException("El nombre de categoría ya está en uso");
            }

            categoria.Nombre = request.Nombre;
            categoria.Estado = request.Estado;

            await _repository.UpdateAsync(categoria);
            await _uow.CommitAsync();
        }

        public async Task DeleteAsync(int id)
        {
            var categoria = await _repository.GetByIdAsync(id);
            if (categoria == null) throw new KeyNotFoundException($"Categoria with id {id} not found.");

            await _repository.DeleteAsync(categoria);
            await _uow.CommitAsync();
        }

        private static CategoriaResponse MapToResponse(Categoria c) => new CategoriaResponse
        {
            CategoriaId = c.CategoriaId,
            Nombre = c.Nombre,
            Estado = c.Estado
        };
    }
}
