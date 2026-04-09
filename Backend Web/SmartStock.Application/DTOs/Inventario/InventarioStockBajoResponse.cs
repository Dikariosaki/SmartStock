namespace SmartStock.Application.DTOs
{
    public class InventarioStockBajoResponse
    {
        public int InventarioId { get; set; }
        public int ProductoId { get; set; }
        public string Ubicacion { get; set; } = string.Empty;
        public int Cantidad { get; set; }
        public int PuntoReorden { get; set; }
        public bool Estado { get; set; }
        public string ProductoNombre { get; set; } = string.Empty;
        public string ProductoCodigo { get; set; } = string.Empty;
    }
}
