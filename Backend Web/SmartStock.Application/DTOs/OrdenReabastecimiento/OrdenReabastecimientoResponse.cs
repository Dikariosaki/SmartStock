namespace SmartStock.Application.DTOs
{
    public class OrdenReabastecimientoResponse
    {
        public int OrdenId { get; set; }
        public int ProveedorId { get; set; }
        public DateTime FechaCreacion { get; set; }
        public string Estado { get; set; } = string.Empty;
    }
}
