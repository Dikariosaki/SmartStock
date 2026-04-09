namespace SmartStock.Application.DTOs
{
    public class SubcategoriaResponse
    {
        public int SubcategoriaId { get; set; }
        public int CategoriaId { get; set; }
        public string CategoriaNombre { get; set; } = string.Empty;
        public string Nombre { get; set; } = string.Empty;
        public bool Estado { get; set; }
    }
}
