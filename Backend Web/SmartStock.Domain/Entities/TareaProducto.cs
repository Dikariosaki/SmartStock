namespace SmartStock.Domain.Entities;

public class TareaProducto
{
    public int TareaId { get; set; }
    public int ProductoId { get; set; }
    public int Cantidad { get; set; }
    public bool Estado { get; set; } = true;
}