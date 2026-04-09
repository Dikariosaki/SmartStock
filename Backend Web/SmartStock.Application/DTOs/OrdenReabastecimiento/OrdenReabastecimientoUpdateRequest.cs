using System.ComponentModel.DataAnnotations;

namespace SmartStock.Application.DTOs
{
    public class OrdenReabastecimientoUpdateRequest
    {
        [Required]
        public int ProveedorId { get; set; }

        [Required]
        [MaxLength(50)]
        public string Estado { get; set; } = string.Empty;
    }
}
