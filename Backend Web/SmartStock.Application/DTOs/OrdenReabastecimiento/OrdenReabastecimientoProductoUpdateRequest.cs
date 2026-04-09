using System.ComponentModel.DataAnnotations;

namespace SmartStock.Application.DTOs
{
    public class OrdenReabastecimientoProductoUpdateRequest
    {
        [Range(1, int.MaxValue)]
        public int CantidadPedida { get; set; }

        [Required]
        public decimal PrecioCompraUnitario { get; set; }

        public bool Estado { get; set; } = true;
    }
}
