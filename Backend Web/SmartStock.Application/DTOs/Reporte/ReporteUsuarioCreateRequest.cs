using System.ComponentModel.DataAnnotations;

namespace SmartStock.Application.DTOs
{
    public class ReporteUsuarioCreateRequest
    {
        [Required]
        public int ReporteId { get; set; }

        [Required]
        public int UsuarioId { get; set; }
    }
}
