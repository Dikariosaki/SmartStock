using System.ComponentModel.DataAnnotations;

namespace SmartStock.Application.DTOs
{
    public class TareaProductoUpdateRequest
    {
        [Range(1, int.MaxValue)]
        public int Cantidad { get; set; }

        public bool Estado { get; set; } = true;
    }
}
