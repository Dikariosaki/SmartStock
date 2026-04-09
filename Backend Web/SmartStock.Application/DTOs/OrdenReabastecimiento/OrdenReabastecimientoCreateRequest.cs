using System.ComponentModel.DataAnnotations;

namespace SmartStock.Application.DTOs
{
    public class OrdenReabastecimientoCreateRequest
    {
        [Required]
        public int ProveedorId { get; set; }

        [Required]
        [MaxLength(50)]
        public string Estado { get; set; } = string.Empty;
    }
}
