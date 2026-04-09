using System.ComponentModel.DataAnnotations;

namespace SmartStock.Application.DTOs
{
    public class ReporteTareaCreateRequest
    {
        [Required]
        public int ReporteId { get; set; }

        [Required]
        public int TareaId { get; set; }
    }
}
