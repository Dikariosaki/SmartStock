using System.ComponentModel.DataAnnotations;

namespace SmartStock.Application.DTOs
{
    public class CategoriaUpdateRequest
    {
        [Required]
        [MaxLength(100)]
        public string Nombre { get; set; } = string.Empty;

        public bool Estado { get; set; } = true;
    }
}
