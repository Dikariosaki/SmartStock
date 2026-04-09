using System.ComponentModel.DataAnnotations;

namespace SmartStock.Application.DTOs
{
    public class SubcategoriaCreateRequest
    {
        [Required]
        public int CategoriaId { get; set; }

        [Required]
        [MaxLength(100)]
        public string Nombre { get; set; } = string.Empty;

        public bool Estado { get; set; } = true;
    }
}
