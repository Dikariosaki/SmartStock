using System.ComponentModel.DataAnnotations;

namespace SmartStock.Application.DTOs
{
    public class ReporteCreateRequest
    {
        [Required]
        [MaxLength(200)]
        public string Titulo { get; set; } = string.Empty;

        public string? Descripcion { get; set; }

        [Required]
        [MaxLength(50)]
        public string TipoReporte { get; set; } = string.Empty;

        public bool Estado { get; set; } = true;
    }
}
