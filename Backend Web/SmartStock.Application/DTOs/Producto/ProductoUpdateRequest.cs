using System.ComponentModel.DataAnnotations;

namespace SmartStock.Application.DTOs
{
    public class ProductoUpdateRequest
    {
        [Required]
        public int SubcategoriaId { get; set; }

        [Required]
        [MaxLength(50)]
        public string Codigo { get; set; } = string.Empty;

        [Required]
        [MaxLength(150)]
        public string Nombre { get; set; } = string.Empty;

        public string? Descripcion { get; set; }

        [Required]
        public decimal PrecioUnitario { get; set; }

        public bool Estado { get; set; } = true;
    }
}
