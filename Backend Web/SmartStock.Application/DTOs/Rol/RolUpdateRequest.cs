using System.ComponentModel.DataAnnotations;

namespace SmartStock.Application.DTOs
{
    public class RolUpdateRequest
    {
        [Required]
        [MaxLength(50)]
        public string Nombre { get; set; } = string.Empty;
    }
}
