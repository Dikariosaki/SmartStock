namespace SmartStock.Domain.Entities;

public class Inventario
{
    public int InventarioId { get; set; }
    public int ProductoId { get; set; }
    public string Ubicacion { get; set; } = string.Empty;
    public int Cantidad { get; set; }
    public int PuntoReorden { get; set; }
    public bool Estado { get; set; } = true;
}