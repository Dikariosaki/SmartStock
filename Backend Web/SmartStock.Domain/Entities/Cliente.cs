namespace SmartStock.Domain.Entities;

public class Cliente
{
    public int ClienteId { get; set; }
    public int? UsuarioId { get; set; }
    public string? Contacto { get; set; }
    public string? Direccion { get; set; }
    public string? Sucursal { get; set; }

    public Usuario? Usuario { get; set; }
}