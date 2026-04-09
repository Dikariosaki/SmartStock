namespace SmartStock.Application.DTOs
{
    public class UsuarioResponse
    {
        public int UsuarioId { get; set; }
        public int RolId { get; set; }
        public string Nombre { get; set; } = string.Empty;
        public int Cedula { get; set; }
        public string Email { get; set; } = string.Empty;
        public string Telefono { get; set; } = string.Empty;
        public bool Estado { get; set; }
    }
}
