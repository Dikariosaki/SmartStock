using Microsoft.Extensions.Configuration;
using SmartStock.Application.DTOs;
using SmartStock.Application.DTOs.Common;
using SmartStock.Application.Interfaces;
using SmartStock.Domain.Entities;
using System.Net;
using System.Linq.Expressions;
using System.Text;
using System.Text.Json;

namespace SmartStock.Application.Services
{
    public class ReporteService : IReporteService
    {
        private readonly IReporteRepository _reporteRepository;
        private readonly IReporteMovimientoRepository _reporteMovimientoRepository;
        private readonly IReporteTareaRepository _reporteTareaRepository;
        private readonly IReporteUsuarioRepository _reporteUsuarioRepository;
        private readonly IUnitOfWork _uow;
        private static readonly JsonSerializerOptions EvidenceJsonOptions = new(JsonSerializerDefaults.Web);
        private static readonly HttpClient StorageHttpClient = new();
        private readonly string _storageBucket;
        private readonly string _storagePublicBaseUrl;

        public ReporteService(
            IReporteRepository reporteRepository,
            IReporteMovimientoRepository reporteMovimientoRepository,
            IReporteTareaRepository reporteTareaRepository,
            IReporteUsuarioRepository reporteUsuarioRepository,
            IUnitOfWork uow,
            IConfiguration configuration)
        {
            _reporteRepository = reporteRepository;
            _reporteMovimientoRepository = reporteMovimientoRepository;
            _reporteTareaRepository = reporteTareaRepository;
            _reporteUsuarioRepository = reporteUsuarioRepository;
            _uow = uow;
            _storageBucket = configuration["Storage:Bucket"] ?? "reportes-evidencia";
            _storagePublicBaseUrl = (configuration["Storage:PublicBaseUrl"] ?? "http://localhost:9000").TrimEnd('/');
        }

        // Reportes
        public async Task<IEnumerable<ReporteResponse>> GetAllAsync()
        {
            var items = await _reporteRepository.ListAsync();
            return items.Select(MapToResponse);
        }

        public async Task<PagedResponse<ReporteResponse>> GetPagedAsync(PagedRequest request)
        {
            Expression<Func<Reporte, bool>> predicate = r =>
                (!request.Estado.HasValue || r.Estado == request.Estado.Value);

            var (items, totalCount) = await _reporteRepository.GetPagedAsync(
                request.PageNumber,
                request.PageSize,
                predicate,
                orderBy: q => q.OrderByDescending(r => r.FechaCreado));

            var dtos = items.Select(MapToResponse);
            return new PagedResponse<ReporteResponse>(dtos, request.PageNumber, request.PageSize, totalCount);
        }

        public async Task<ReporteResponse?> GetByIdAsync(int id)
        {
            var item = await _reporteRepository.GetByIdAsync(id);
            return item == null ? null : MapToResponse(item);
        }

        public async Task<ReporteEvidenciaImagenContenido?> GetEvidenceImageAsync(int id, int imageIndex)
        {
            var entity = await _reporteRepository.GetByIdAsync(id);
            if (entity is null)
            {
                return null;
            }

            var storedEvidence = DeserializeStoredEvidence(entity.EvidenciaJson);
            var reference = storedEvidence?.AllImageReferences().ElementAtOrDefault(imageIndex);
            if (string.IsNullOrWhiteSpace(reference))
            {
                return null;
            }

            return await DownloadEvidenceImageAsync(reference) ?? BuildPlaceholderEvidenceImage(entity.Titulo, imageIndex);
        }

        public async Task<ReporteResponse> CreateAsync(ReporteCreateRequest request)
        {
            var entity = new Reporte
            {
                Titulo = request.Titulo,
                Descripcion = request.Descripcion,
                FechaCreado = DateTime.UtcNow.AddHours(-5),
                TipoReporte = request.TipoReporte,
                Estado = request.Estado
            };

            await _reporteRepository.AddAsync(entity);
            await _uow.CommitAsync();

            return MapToResponse(entity);
        }

        public async Task UpdateAsync(int id, ReporteUpdateRequest request)
        {
            var entity = await _reporteRepository.GetByIdAsync(id);
            if (entity == null)
                throw new KeyNotFoundException($"Reporte con ID {id} no encontrado");

            entity.Titulo = request.Titulo;
            entity.Descripcion = request.Descripcion;
            entity.TipoReporte = request.TipoReporte;
            entity.Estado = request.Estado;

            await _reporteRepository.UpdateAsync(entity);
            await _uow.CommitAsync();
        }

        public async Task DeleteAsync(int id)
        {
            var entity = await _reporteRepository.GetByIdAsync(id);
            if (entity == null)
                throw new KeyNotFoundException($"Reporte con ID {id} no encontrado");

            await _reporteRepository.DeleteAsync(entity);
            await _uow.CommitAsync();
        }

        public async Task ActivateAsync(int id)
        {
            var entity = await _reporteRepository.GetByIdAsync(id);
            if (entity == null)
                throw new KeyNotFoundException($"Reporte con ID {id} no encontrado");

            entity.Estado = true;
            await _reporteRepository.UpdateAsync(entity);
            await _uow.CommitAsync();
        }

        public async Task DeactivateAsync(int id)
        {
            var entity = await _reporteRepository.GetByIdAsync(id);
            if (entity == null)
                throw new KeyNotFoundException($"Reporte con ID {id} no encontrado");

            entity.Estado = false;
            await _reporteRepository.UpdateAsync(entity);
            await _uow.CommitAsync();
        }

        // ReporteMovimientos
        public async Task<IEnumerable<ReporteMovimientoResponse>> GetMovimientosByReporteAsync(int reporteId)
        {
            var items = await _reporteMovimientoRepository.ListByReporteAsync(reporteId);
            return items.Select(MapToResponse);
        }

        public async Task<IEnumerable<ReporteMovimientoResponse>> GetReportesByMovimientoAsync(int movimientoId)
        {
            var items = await _reporteMovimientoRepository.ListByMovimientoAsync(movimientoId);
            return items.Select(MapToResponse);
        }

        public async Task<ReporteMovimientoResponse> AddMovimientoAsync(ReporteMovimientoCreateRequest request)
        {
            var exists = await _reporteMovimientoRepository.GetByIdsAsync(request.ReporteId, request.MovimientoId);
            if (exists != null)
                throw new InvalidOperationException("El movimiento ya está asociado a este reporte");

            var entity = new ReporteMovimiento
            {
                ReporteId = request.ReporteId,
                MovimientoId = request.MovimientoId
            };

            await _reporteMovimientoRepository.AddAsync(entity);
            await _uow.CommitAsync();

            return MapToResponse(entity);
        }

        public async Task RemoveMovimientoAsync(int reporteId, int movimientoId)
        {
            var entity = await _reporteMovimientoRepository.GetByIdsAsync(reporteId, movimientoId);
            if (entity == null)
                throw new KeyNotFoundException("Relación Reporte-Movimiento no encontrada");

            await _reporteMovimientoRepository.DeleteAsync(entity);
            await _uow.CommitAsync();
        }

        // ReporteTareas
        public async Task<IEnumerable<ReporteTareaResponse>> GetTareasByReporteAsync(int reporteId)
        {
            var items = await _reporteTareaRepository.ListByReporteAsync(reporteId);
            return items.Select(MapToResponse);
        }

        public async Task<IEnumerable<ReporteTareaResponse>> GetReportesByTareaAsync(int tareaId)
        {
            var items = await _reporteTareaRepository.ListByTareaAsync(tareaId);
            return items.Select(MapToResponse);
        }

        public async Task<ReporteTareaResponse> AddTareaAsync(ReporteTareaCreateRequest request)
        {
            var exists = await _reporteTareaRepository.GetByIdsAsync(request.ReporteId, request.TareaId);
            if (exists != null)
                throw new InvalidOperationException("La tarea ya está asociada a este reporte");

            var entity = new ReporteTarea
            {
                ReporteId = request.ReporteId,
                TareaId = request.TareaId
            };

            await _reporteTareaRepository.AddAsync(entity);
            await _uow.CommitAsync();

            return MapToResponse(entity);
        }

        public async Task RemoveTareaAsync(int reporteId, int tareaId)
        {
            var entity = await _reporteTareaRepository.GetByIdsAsync(reporteId, tareaId);
            if (entity == null)
                throw new KeyNotFoundException("Relación Reporte-Tarea no encontrada");

            await _reporteTareaRepository.DeleteAsync(entity);
            await _uow.CommitAsync();
        }

        // ReporteUsuarios
        public async Task<IEnumerable<ReporteUsuarioResponse>> GetUsuariosByReporteAsync(int reporteId)
        {
            var items = await _reporteUsuarioRepository.ListByReporteAsync(reporteId);
            return items.Select(MapToResponse);
        }

        public async Task<IEnumerable<ReporteUsuarioResponse>> GetReportesByUsuarioAsync(int usuarioId)
        {
            var items = await _reporteUsuarioRepository.ListByUsuarioAsync(usuarioId);
            return items.Select(MapToResponse);
        }

        public async Task<ReporteUsuarioResponse> AddUsuarioAsync(ReporteUsuarioCreateRequest request)
        {
            var exists = await _reporteUsuarioRepository.GetByIdsAsync(request.ReporteId, request.UsuarioId);
            if (exists != null)
                throw new InvalidOperationException("El usuario ya está asociado a este reporte");

            var entity = new ReporteUsuario
            {
                ReporteId = request.ReporteId,
                UsuarioId = request.UsuarioId
            };

            await _reporteUsuarioRepository.AddAsync(entity);
            await _uow.CommitAsync();

            return MapToResponse(entity);
        }

        public async Task RemoveUsuarioAsync(int reporteId, int usuarioId)
        {
            var entity = await _reporteUsuarioRepository.GetByIdsAsync(reporteId, usuarioId);
            if (entity == null)
                throw new KeyNotFoundException("Relación Reporte-Usuario no encontrada");

            await _reporteUsuarioRepository.DeleteAsync(entity);
            await _uow.CommitAsync();
        }

        private ReporteResponse MapToResponse(Reporte entity)
        {
            return new ReporteResponse
            {
                ReporteId = entity.ReporteId,
                Titulo = entity.Titulo,
                Descripcion = entity.Descripcion,
                Evidencia = DeserializeEvidence(entity),
                FechaCreado = entity.FechaCreado,
                TipoReporte = entity.TipoReporte,
                Estado = entity.Estado
            };
        }

        private ReporteEvidenciaDto? DeserializeEvidence(Reporte entity)
        {
            var storedEvidence = DeserializeStoredEvidence(entity.EvidenciaJson);
            if (storedEvidence is null)
            {
                return null;
            }

            return new ReporteEvidenciaDto
            {
                ImageUrls = storedEvidence.AllImageReferences().Select((_, index) => BuildEvidenceImagePath(entity.ReporteId, index)).ToList(),
                Observation = storedEvidence.Observation
            };
        }

        private StoredReporteEvidenciaDto? DeserializeStoredEvidence(string? evidenciaJson)
        {
            if (string.IsNullOrWhiteSpace(evidenciaJson))
            {
                return null;
            }

            try
            {
                return JsonSerializer.Deserialize<StoredReporteEvidenciaDto>(evidenciaJson, EvidenceJsonOptions);
            }
            catch (JsonException)
            {
                return null;
            }
        }

        private async Task<ReporteEvidenciaImagenContenido?> DownloadEvidenceImageAsync(string reference)
        {
            var trimmedReference = reference.Trim();
            if (string.IsNullOrWhiteSpace(trimmedReference))
            {
                return null;
            }

            var objectKey = ExtractObjectKey(trimmedReference);
            var sourceUrl = !string.IsNullOrWhiteSpace(objectKey)
                ? $"{_storagePublicBaseUrl}/{_storageBucket}/{objectKey}"
                : trimmedReference.Contains("://", StringComparison.Ordinal)
                    ? trimmedReference
                    : $"{_storagePublicBaseUrl}/{_storageBucket}/{trimmedReference.TrimStart('/')}";

            try
            {
                using var response = await StorageHttpClient.GetAsync(sourceUrl);
                if (!response.IsSuccessStatusCode)
                {
                    return null;
                }

                var contentType = response.Content.Headers.ContentType?.MediaType;
                return new ReporteEvidenciaImagenContenido
                {
                    Content = await response.Content.ReadAsByteArrayAsync(),
                    ContentType = string.IsNullOrWhiteSpace(contentType) ? InferContentType(objectKey ?? trimmedReference) : contentType
                };
            }
            catch (HttpRequestException)
            {
                return null;
            }
        }

        private string? ExtractObjectKey(string reference)
        {
            var trimmedReference = reference.Trim();
            if (string.IsNullOrWhiteSpace(trimmedReference))
            {
                return null;
            }

            if (!trimmedReference.Contains("://", StringComparison.Ordinal))
            {
                var normalizedReference = trimmedReference.TrimStart('/');
                if (normalizedReference.StartsWith($"{_storageBucket}/", StringComparison.OrdinalIgnoreCase))
                {
                    normalizedReference = normalizedReference[(_storageBucket.Length + 1)..];
                }

                return string.IsNullOrWhiteSpace(normalizedReference) ? null : normalizedReference;
            }

            if (!Uri.TryCreate(trimmedReference, UriKind.Absolute, out var uri))
            {
                return null;
            }

            var bucketMarker = $"/{_storageBucket}/";
            var path = uri.AbsolutePath;
            var bucketIndex = path.IndexOf(bucketMarker, StringComparison.OrdinalIgnoreCase);
            if (bucketIndex < 0)
            {
                return null;
            }

            var objectKey = path[(bucketIndex + bucketMarker.Length)..].TrimStart('/');
            return string.IsNullOrWhiteSpace(objectKey) ? null : objectKey;
        }

        private static string BuildEvidenceImagePath(int reporteId, int imageIndex) =>
            $"/api/Reportes/{reporteId}/evidencia/imagenes/{imageIndex}";

        private static string InferContentType(string value)
        {
            var normalized = value.Trim().ToLowerInvariant();
            return normalized switch
            {
                var png when png.EndsWith(".png") => "image/png",
                var jpg when jpg.EndsWith(".jpg") || jpg.EndsWith(".jpeg") => "image/jpeg",
                var gif when gif.EndsWith(".gif") => "image/gif",
                var svg when svg.EndsWith(".svg") => "image/svg+xml",
                _ => "image/webp"
            };
        }

        private static ReporteEvidenciaImagenContenido BuildPlaceholderEvidenceImage(string titulo, int imageIndex)
        {
            var safeTitle = WebUtility.HtmlEncode(titulo);
            var svg =
                $$"""
                <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 1280 720">
                  <rect width="1280" height="720" fill="#0f172a" />
                  <rect x="72" y="72" width="1136" height="576" rx="36" fill="#1e293b" />
                  <text x="120" y="220" fill="#e2e8f0" font-size="48" font-family="Segoe UI, Arial, sans-serif" font-weight="700">Evidencia no disponible</text>
                  <text x="120" y="304" fill="#94a3b8" font-size="32" font-family="Segoe UI, Arial, sans-serif">{{safeTitle}}</text>
                  <text x="120" y="360" fill="#94a3b8" font-size="28" font-family="Segoe UI, Arial, sans-serif">Imagen {{imageIndex + 1}}</text>
                  <text x="120" y="416" fill="#94a3b8" font-size="28" font-family="Segoe UI, Arial, sans-serif">El blob asociado no se encontro en RustFS.</text>
                </svg>
                """;

            return new ReporteEvidenciaImagenContenido
            {
                Content = Encoding.UTF8.GetBytes(svg),
                ContentType = "image/svg+xml"
            };
        }

        private static ReporteMovimientoResponse MapToResponse(ReporteMovimiento entity)
        {
            return new ReporteMovimientoResponse
            {
                ReporteId = entity.ReporteId,
                MovimientoId = entity.MovimientoId
            };
        }

        private static ReporteTareaResponse MapToResponse(ReporteTarea entity)
        {
            return new ReporteTareaResponse
            {
                ReporteId = entity.ReporteId,
                TareaId = entity.TareaId
            };
        }

        private static ReporteUsuarioResponse MapToResponse(ReporteUsuario entity)
        {
            return new ReporteUsuarioResponse
            {
                ReporteId = entity.ReporteId,
                UsuarioId = entity.UsuarioId
            };
        }
    }
}
