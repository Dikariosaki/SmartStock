namespace SmartStock.Application.DTOs
{
    public class TareaResponse
    {
        public int TareaId { get; set; }
        public string Titulo { get; set; } = string.Empty;
        public string? Descripcion { get; set; }
        public int? AsignadoA { get; set; }
        public string? UsuarioNombre { get; set; }
        public DateTime FechaCreacion { get; set; }
        public DateTime? FechaFin { get; set; }
        public bool Estado { get; set; }
    }
}
