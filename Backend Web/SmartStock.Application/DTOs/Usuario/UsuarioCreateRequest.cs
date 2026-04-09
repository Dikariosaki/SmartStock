using System.ComponentModel.DataAnnotations;

namespace SmartStock.Application.DTOs
{
    public class UsuarioCreateRequest
    {
        [Required]
        public int RolId { get; set; }

        [Required]
        [MaxLength(100)]
        public string Nombre { get; set; } = string.Empty;

        [Required]
        public int Cedula { get; set; }

        [Required]
        [MaxLength(150)]
        [EmailAddress]
        public string Email { get; set; } = string.Empty;

        [MaxLength(100)]
        public string? Password { get; set; }

        [MaxLength(50)]
        public string? Telefono { get; set; }

        public bool Estado { get; set; } = true;
    }
}
