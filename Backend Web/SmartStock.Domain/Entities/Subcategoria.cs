namespace SmartStock.Domain.Entities;

public class Subcategoria
{
    public int SubcategoriaId { get; set; }
    public int CategoriaId { get; set; }
    public string Nombre { get; set; } = string.Empty;
    public bool Estado { get; set; } = true;
}