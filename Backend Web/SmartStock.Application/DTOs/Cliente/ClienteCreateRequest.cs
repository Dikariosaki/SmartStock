using System.ComponentModel.DataAnnotations;

namespace SmartStock.Application.DTOs
{
    public class ClienteCreateRequest
    {
        public int? UsuarioId { get; set; }

        [MaxLength(150)]
        public string? Contacto { get; set; }

        [MaxLength(150)]
        public string? Direccion { get; set; }

        [MaxLength(150)]
        public string? Sucursal { get; set; }
    }
}
