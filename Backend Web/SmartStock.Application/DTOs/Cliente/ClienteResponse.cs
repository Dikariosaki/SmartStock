namespace SmartStock.Application.DTOs
{
    public class ClienteResponse
    {
        public int ClienteId { get; set; }
        public int? UsuarioId { get; set; }
        public string? Contacto { get; set; }
        public string? Direccion { get; set; }
        public string? Sucursal { get; set; }
        public UsuarioResponse? Usuario { get; set; }
    }
}
