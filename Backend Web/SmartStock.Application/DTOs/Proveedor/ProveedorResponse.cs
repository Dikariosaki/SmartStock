namespace SmartStock.Application.DTOs
{
    public class ProveedorResponse
    {
        public int ProveedorId { get; set; }
        public int? UsuarioId { get; set; }
        public string? Contacto { get; set; }
        public UsuarioResponse? Usuario { get; set; }
    }
}
