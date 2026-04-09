using System.ComponentModel.DataAnnotations;

namespace SmartStock.Application.DTOs
{
    public class ProveedorUpdateRequest
    {
        public int? UsuarioId { get; set; }

        [MaxLength(150)]
        public string? Contacto { get; set; }
    }
}
