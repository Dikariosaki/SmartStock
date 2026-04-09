using System.ComponentModel.DataAnnotations;

namespace SmartStock.Application.DTOs
{
    public class InventarioUpdateRequest
    {
        [Required]
        public int ProductoId { get; set; }

        [Required]
        [MaxLength(100)]
        public string Ubicacion { get; set; } = string.Empty;

        [Range(0, int.MaxValue)]
        public int Cantidad { get; set; }

        [Range(0, int.MaxValue)]
        public int PuntoReorden { get; set; }

        public bool Estado { get; set; } = true;
    }
}
