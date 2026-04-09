using Microsoft.Extensions.DependencyInjection;
using SmartStock.Application.Interfaces;
using SmartStock.Application.Services;

namespace SmartStock.Application;

public static class DependencyInjection
{
    public static IServiceCollection AddApplication(this IServiceCollection services)
    {
        services.AddScoped<IProductoService, ProductoService>();
        services.AddScoped<IInventarioService, InventarioService>();
        services.AddScoped<IClienteService, ClienteService>();
        services.AddScoped<IProveedorService, ProveedorService>();
        services.AddScoped<ICategoriaService, CategoriaService>();
        services.AddScoped<ISubcategoriaService, SubcategoriaService>();
        services.AddScoped<IUsuarioService, UsuarioService>();
        services.AddScoped<IAuthService, AuthService>();
        services.AddScoped<IRolService, RolService>();
        services.AddScoped<IMovimientoService, MovimientoService>();
        services.AddScoped<IOrdenReabastecimientoService, OrdenReabastecimientoService>();
        services.AddScoped<ITareaService, TareaService>();
        services.AddScoped<IReporteService, ReporteService>();
        return services;
    }
}
