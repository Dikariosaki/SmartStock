using System.ComponentModel.DataAnnotations;

namespace SmartStock.Application.DTOs
{
    public class ProductoCreateRequest
    {
        [Required]
        public int SubcategoriaId { get; set; }

        [MaxLength(50)]
        public string? Codigo { get; set; }

        [Required]
        [MaxLength(150)]
        public string Nombre { get; set; } = string.Empty;

        public string? Descripcion { get; set; }

        [Required]
        public decimal PrecioUnitario { get; set; }

        public bool Estado { get; set; } = true;
    }
}
