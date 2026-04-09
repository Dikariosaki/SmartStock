using System.ComponentModel.DataAnnotations;

namespace SmartStock.Application.DTOs
{
    public class TareaUpdateRequest
    {
        [Required]
        [MaxLength(200)]
        public string Titulo { get; set; } = string.Empty;

        public string? Descripcion { get; set; }

        public int? AsignadoA { get; set; }

        public DateTime? FechaFin { get; set; }

        public bool Estado { get; set; } = true;
    }
}
