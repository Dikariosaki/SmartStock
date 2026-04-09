using System.ComponentModel.DataAnnotations;

namespace SmartStock.Application.DTOs
{
    public class ReporteMovimientoCreateRequest
    {
        [Required]
        public int ReporteId { get; set; }

        [Required]
        public int MovimientoId { get; set; }
    }
}
