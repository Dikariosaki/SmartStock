namespace SmartStock.Domain.Entities;

public class Tarea
{
    public int TareaId { get; set; }
    public string Titulo { get; set; } = string.Empty;
    public string? Descripcion { get; set; }
    public int? AsignadoA { get; set; }
    public DateTime FechaCreacion { get; set; }
    public DateTime? FechaFin { get; set; }
    public bool Estado { get; set; } = true;
}