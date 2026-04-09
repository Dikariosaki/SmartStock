namespace SmartStock.Application.DTOs
{
    public sealed class ReporteEvidenciaImagenContenido
    {
        public required byte[] Content { get; init; }
        public required string ContentType { get; init; }
    }
}
