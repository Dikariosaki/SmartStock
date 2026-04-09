namespace SmartStock.Domain.Entities;

public class Producto
{
    public int ProductoId { get; set; }
    public int SubcategoriaId { get; set; }
    public string Codigo { get; set; } = string.Empty;
    public string Nombre { get; set; } = string.Empty;
    public string? Descripcion { get; set; }
    public decimal PrecioUnitario { get; set; }
    public bool Estado { get; set; } = true;
}