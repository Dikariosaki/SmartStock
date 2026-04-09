using Microsoft.EntityFrameworkCore;
using Microsoft.Extensions.Configuration;
using Microsoft.Extensions.DependencyInjection;
using SmartStock.Application.Interfaces;
using SmartStock.Infrastructure.Data;
using SmartStock.Infrastructure.Repositories;

namespace SmartStock.Infrastructure;

public static class DependencyInjection
{
    public static IServiceCollection AddInfrastructure(this IServiceCollection services, IConfiguration configuration)
    {
        var connectionString = configuration.GetConnectionString("DefaultConnection");
        services.AddDbContext<SmartStockDbContext>(options =>
            options.UseMySql(connectionString!, ServerVersion.AutoDetect(connectionString)));

        services.AddScoped(typeof(IRepository<>), typeof(Repository<>));
        services.AddScoped<IUsuarioRepository, UsuarioRepository>();
        services.AddScoped<ICategoriaRepository, CategoriaRepository>();
        services.AddScoped<IProductoRepository, ProductoRepository>();
        services.AddScoped<IProveedorRepository, ProveedorRepository>();
        services.AddScoped<IRolRepository, RolRepository>();
        services.AddScoped<ISubcategoriaRepository, SubcategoriaRepository>();
        services.AddScoped<IClienteRepository, ClienteRepository>();
        services.AddScoped<IInventarioRepository, InventarioRepository>();
        services.AddScoped<IOrdenReabastecimientoRepository, OrdenReabastecimientoRepository>();
        services.AddScoped<IMovimientoRepository, MovimientoRepository>();
        services.AddScoped<IReporteRepository, ReporteRepository>();
        services.AddScoped<ITareaRepository, TareaRepository>();
        services.AddScoped<IOrdenReabastecimientoProductoRepository, OrdenReabastecimientoProductoRepository>();
        services.AddScoped<IReporteUsuarioRepository, ReporteUsuarioRepository>();
        services.AddScoped<IReporteMovimientoRepository, ReporteMovimientoRepository>();
        services.AddScoped<IReporteTareaRepository, ReporteTareaRepository>();
        services.AddScoped<ITareaProductoRepository, TareaProductoRepository>();
        services.AddScoped<IUnitOfWork, UnitOfWork>();

        return services;
    }
}
