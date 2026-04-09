namespace SmartStock.Domain.Entities;

public class Proveedor
{
    public int ProveedorId { get; set; }
    public int? UsuarioId { get; set; }
    public string? Contacto { get; set; }

    // Navigation property
    public Usuario? Usuario { get; set; }
}