namespace SmartStock.Domain.Entities;

public class OrdenReabastecimientoProducto
{
    public int OrdenId { get; set; }
    public int ProductoId { get; set; }
    public int CantidadPedida { get; set; }
    public decimal PrecioCompraUnitario { get; set; }
    public bool Estado { get; set; } = true;
}