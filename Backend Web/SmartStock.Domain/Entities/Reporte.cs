namespace SmartStock.Domain.Entities;

public class Reporte
{
    public int ReporteId { get; set; }
    public string Titulo { get; set; } = string.Empty;
    public string? Descripcion { get; set; }
    public string? EvidenciaJson { get; set; }
    public DateTime FechaCreado { get; set; }
    public string TipoReporte { get; set; } = string.Empty;
    public bool Estado { get; set; } = true;
}
