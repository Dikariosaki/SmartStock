namespace SmartStock.Application.DTOs
{
    public class MovimientoResponse
    {
        public int MovimientoId { get; set; }
        public int InventarioId { get; set; }
        public int? OrdenId { get; set; }
        public int UsuarioId { get; set; }
        public int? ProveedorId { get; set; }
        public int? ClienteId { get; set; }
        public string Tipo { get; set; } = string.Empty;
        public int Cantidad { get; set; }
        public DateTime FechaMovimiento { get; set; }
        public string? Lote { get; set; }
        public bool Estado { get; set; }

        // Campos adicionales para reportes
        public string? ProductoNombre { get; set; }
        public string? ProductoCodigo { get; set; }
        public string? UsuarioNombre { get; set; }
        public string? ProveedorNombre { get; set; }
        public string? ClienteNombre { get; set; }
    }
}
