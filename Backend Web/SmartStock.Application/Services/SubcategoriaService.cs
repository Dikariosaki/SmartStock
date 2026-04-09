using SmartStock.Application.DTOs;
using SmartStock.Application.DTOs.Common;
using SmartStock.Application.Interfaces;
using SmartStock.Domain.Entities;
using System.Linq.Expressions;

namespace SmartStock.Application.Services
{
    public class SubcategoriaService : ISubcategoriaService
    {
        private readonly ISubcategoriaRepository _repository;
        private readonly ICategoriaRepository _categoriaRepository;
        private readonly IUnitOfWork _uow;

        public SubcategoriaService(ISubcategoriaRepository repository, ICategoriaRepository categoriaRepository, IUnitOfWork uow)
        {
            _repository = repository;
            _categoriaRepository = categoriaRepository;
            _uow = uow;
        }

        public async Task<IEnumerable<SubcategoriaResponse>> GetAllAsync()
        {
            var subs = await _repository.ListAsync();
            var categorias = await _categoriaRepository.ListAsync();
            var categoriaDict = categorias.ToDictionary(c => c.CategoriaId, c => c.Nombre);

            return subs.Select(s => MapToResponse(s, categoriaDict.ContainsKey(s.CategoriaId) ? categoriaDict[s.CategoriaId] : string.Empty));
        }

        public async Task<PagedResponse<SubcategoriaResponse>> GetPagedAsync(PagedRequest request)
        {
            Expression<Func<Subcategoria, bool>> predicate = s =>
                (!request.Estado.HasValue || s.Estado == request.Estado.Value);

            var (items, totalCount) = await _repository.GetPagedAsync(
                request.PageNumber,
                request.PageSize,
                predicate,
                orderBy: q => q.OrderBy(s => s.Nombre));

            var categorias = await _categoriaRepository.ListAsync(); // Optimization: could fetch only needed IDs
            var categoriaDict = categorias.ToDictionary(c => c.CategoriaId, c => c.Nombre);

            var dtos = items.Select(s => MapToResponse(s, categoriaDict.ContainsKey(s.CategoriaId) ? categoriaDict[s.CategoriaId] : string.Empty));
            return new PagedResponse<SubcategoriaResponse>(dtos, request.PageNumber, request.PageSize, totalCount);
        }

        public async Task<SubcategoriaResponse?> GetByIdAsync(int id)
        {
            var sub = await _repository.GetByIdAsync(id);
            if (sub == null) return null;

            var categoria = await _categoriaRepository.GetByIdAsync(sub.CategoriaId);
            return MapToResponse(sub, categoria?.Nombre ?? string.Empty);
        }

        public async Task<IEnumerable<SubcategoriaResponse>> GetByCategoriaIdAsync(int categoriaId)
        {
            var subs = await _repository.GetByCategoriaAsync(categoriaId);
            var categoria = await _categoriaRepository.GetByIdAsync(categoriaId);
            var nombreCategoria = categoria?.Nombre ?? string.Empty;

            return subs.Select(s => MapToResponse(s, nombreCategoria));
        }

        public async Task<SubcategoriaResponse> CreateAsync(SubcategoriaCreateRequest request)
        {
            var categoria = await _categoriaRepository.GetByIdAsync(request.CategoriaId);
            if (categoria == null) throw new ArgumentException("La categoría especificada no existe");

            if (await _repository.NombreExisteEnCategoriaAsync(request.CategoriaId, request.Nombre))
                throw new InvalidOperationException("El nombre ya existe en la categoría");

            var sub = new Subcategoria
            {
                CategoriaId = request.CategoriaId,
                Nombre = request.Nombre,
                Estado = request.Estado
            };

            await _repository.AddAsync(sub);
            await _uow.CommitAsync();

            return MapToResponse(sub, categoria.Nombre);
        }

        public async Task UpdateAsync(int id, SubcategoriaUpdateRequest request)
        {
            var sub = await _repository.GetByIdAsync(id);
            if (sub == null) throw new KeyNotFoundException($"Subcategoria with id {id} not found.");

            var categoria = await _categoriaRepository.GetByIdAsync(request.CategoriaId);
            if (categoria == null) throw new ArgumentException("La categoría especificada no existe");

            if (sub.CategoriaId != request.CategoriaId || !string.Equals(sub.Nombre, request.Nombre, StringComparison.OrdinalIgnoreCase))
            {
                if (await _repository.NombreExisteEnCategoriaAsync(request.CategoriaId, request.Nombre, excludeId: id))
                    throw new InvalidOperationException("El nombre ya existe en la categoría");
            }

            sub.CategoriaId = request.CategoriaId;
            sub.Nombre = request.Nombre;
            sub.Estado = request.Estado;

            await _repository.UpdateAsync(sub);
            await _uow.CommitAsync();
        }

        public async Task ActivarAsync(int id)
        {
            var sub = await _repository.GetByIdAsync(id);
            if (sub == null) throw new KeyNotFoundException($"Subcategoria with id {id} not found.");

            sub.Estado = true;
            await _repository.UpdateAsync(sub);
            await _uow.CommitAsync();
        }

        public async Task DesactivarAsync(int id)
        {
            var sub = await _repository.GetByIdAsync(id);
            if (sub == null) throw new KeyNotFoundException($"Subcategoria with id {id} not found.");

            sub.Estado = false;
            await _repository.UpdateAsync(sub);
            await _uow.CommitAsync();
        }

        public async Task DeleteAsync(int id)
        {
            var sub = await _repository.GetByIdAsync(id);
            if (sub == null) throw new KeyNotFoundException($"Subcategoria with id {id} not found.");

            await _repository.DeleteAsync(sub);
            await _uow.CommitAsync();
        }

        private static SubcategoriaResponse MapToResponse(Subcategoria s, string categoriaNombre) => new SubcategoriaResponse
        {
            SubcategoriaId = s.SubcategoriaId,
            CategoriaId = s.CategoriaId,
            CategoriaNombre = categoriaNombre,
            Nombre = s.Nombre,
            Estado = s.Estado
        };
    }
}
