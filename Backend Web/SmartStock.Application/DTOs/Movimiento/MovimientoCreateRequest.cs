using System.ComponentModel.DataAnnotations;

namespace SmartStock.Application.DTOs
{
    public class MovimientoCreateRequest
    {
        [Required]
        public int InventarioId { get; set; }

        public int? OrdenId { get; set; }

        [Required]
        public int UsuarioId { get; set; }

        public int? ProveedorId { get; set; }
        public int? ClienteId { get; set; }

        [Required]
        [MaxLength(10)]
        public string Tipo { get; set; } = string.Empty;

        [Range(1, int.MaxValue)]
        public int Cantidad { get; set; }

        [MaxLength(100)]
        public string? Lote { get; set; }

        public bool Estado { get; set; } = true;
    }
}
