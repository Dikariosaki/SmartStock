using System.ComponentModel.DataAnnotations;

namespace SmartStock.Application.DTOs
{
    public class MovimientoStockRequest
    {
        [Required]
        public int ProductoId { get; set; }

        [Required]
        [Range(1, int.MaxValue, ErrorMessage = "La cantidad debe ser mayor a 0")]
        public int Cantidad { get; set; }

        public int? ProveedorId { get; set; }
        public int? ClienteId { get; set; }
        public string? Lote { get; set; }

        public int UsuarioId { get; set; } = 1; // Default user ID
    }
}
