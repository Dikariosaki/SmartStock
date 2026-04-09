namespace SmartStock.Application.DTOs
{
    public class ReporteResponse
    {
        public int ReporteId { get; set; }
        public string Titulo { get; set; } = string.Empty;
        public string? Descripcion { get; set; }
        public ReporteEvidenciaDto? Evidencia { get; set; }
        public DateTime FechaCreado { get; set; }
        public string TipoReporte { get; set; } = string.Empty;
        public bool Estado { get; set; }
        public string? UsuarioNombre { get; set; }
    }
}
