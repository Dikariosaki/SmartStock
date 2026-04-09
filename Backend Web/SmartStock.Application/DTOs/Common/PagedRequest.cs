namespace SmartStock.Application.DTOs.Common;

public class PagedRequest
{
    public int PageNumber { get; set; } = 1;
    public int PageSize { get; set; } = 10;
    public bool? Estado { get; set; }
    public string? Tipo { get; set; }
    public string? Search { get; set; }
    public List<string>? RoleNames { get; set; }
}
