using System.ComponentModel.DataAnnotations;

namespace SmartStock.Application.DTOs
{
    public class RolCreateRequest
    {
        [Required]
        [MaxLength(50)]
        public string Nombre { get; set; } = string.Empty;
    }
}
