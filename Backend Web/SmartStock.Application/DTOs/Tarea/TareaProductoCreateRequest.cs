using System.ComponentModel.DataAnnotations;

namespace SmartStock.Application.DTOs
{
    public class TareaProductoCreateRequest
    {
        [Required]
        public int TareaId { get; set; }

        [Required]
        public int ProductoId { get; set; }

        [Range(1, int.MaxValue)]
        public int Cantidad { get; set; }

        public bool Estado { get; set; } = true;
    }
}
