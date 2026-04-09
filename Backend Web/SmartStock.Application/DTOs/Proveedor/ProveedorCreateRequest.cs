using System.ComponentModel.DataAnnotations;

namespace SmartStock.Application.DTOs
{
    public class ProveedorCreateRequest
    {
        public int? UsuarioId { get; set; }

        [MaxLength(150)]
        public string? Contacto { get; set; }
    }
}
