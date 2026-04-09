using Microsoft.EntityFrameworkCore;
using SmartStock.Domain.Entities;

namespace SmartStock.Infrastructure.Data;

public class SmartStockDbContext : DbContext
{
    public SmartStockDbContext(DbContextOptions<SmartStockDbContext> options) : base(options) { }

    public DbSet<Usuario> Usuarios => Set<Usuario>();
    public DbSet<Categoria> Categorias => Set<Categoria>();
    public DbSet<Producto> Productos => Set<Producto>();
    public DbSet<Proveedor> Proveedores => Set<Proveedor>();
    public DbSet<Rol> Roles => Set<Rol>();
    public DbSet<Subcategoria> Subcategorias => Set<Subcategoria>();
    public DbSet<Cliente> Clientes => Set<Cliente>();
    public DbSet<Inventario> Inventarios => Set<Inventario>();
    public DbSet<OrdenReabastecimiento> OrdenesReabastecimiento => Set<OrdenReabastecimiento>();
    public DbSet<OrdenReabastecimientoProducto> OrdenesReabastecimientoProductos => Set<OrdenReabastecimientoProducto>();
    public DbSet<Movimiento> Movimientos => Set<Movimiento>();
    public DbSet<Reporte> Reportes => Set<Reporte>();
    public DbSet<ReporteUsuario> ReporteUsuarios => Set<ReporteUsuario>();
    public DbSet<ReporteMovimiento> ReporteMovimientos => Set<ReporteMovimiento>();
    public DbSet<Tarea> Tareas => Set<Tarea>();
    public DbSet<ReporteTarea> ReporteTareas => Set<ReporteTarea>();
    public DbSet<TareaProducto> TareaProductos => Set<TareaProducto>();

    protected override void OnModelCreating(ModelBuilder modelBuilder)
    {
        base.OnModelCreating(modelBuilder);

        modelBuilder.Entity<Usuario>(entity =>
        {
            entity.ToTable("Usuario");
            entity.HasKey(e => e.UsuarioId);
            entity.Property(e => e.UsuarioId).HasColumnName("usuario_id");
            entity.Property(e => e.RolId).HasColumnName("rol_id");
            entity.Property(e => e.Nombre).HasColumnName("nombre").HasMaxLength(100).IsRequired();
            entity.Property(e => e.Cedula).HasColumnName("Cedula").IsRequired();
            entity.Property(e => e.Email).HasColumnName("email").HasMaxLength(150).IsRequired();
            entity.Property(e => e.PasswordHash).HasColumnName("password_hash").HasMaxLength(255).IsRequired();
            entity.Property(e => e.Estado).HasColumnName("estado");
            entity.Property(e => e.Telefono).HasColumnName("telefono").HasMaxLength(50);

            entity.HasOne(d => d.Rol)
                .WithMany()
                .HasForeignKey(d => d.RolId)
                .OnDelete(DeleteBehavior.Restrict);
        });

        // Categoria
        modelBuilder.Entity<Categoria>(entity =>
        {
            entity.ToTable("Categoria");
            entity.HasKey(e => e.CategoriaId);
            entity.Property(e => e.CategoriaId).HasColumnName("categoria_id");
            entity.Property(e => e.Nombre).HasColumnName("nombre").HasMaxLength(100).IsRequired();
            entity.Property(e => e.Estado).HasColumnName("estado");
        });

        // Producto
        modelBuilder.Entity<Producto>(entity =>
        {
            entity.ToTable("Producto");
            entity.HasKey(e => e.ProductoId);
            entity.Property(e => e.ProductoId).HasColumnName("producto_id");
            entity.Property(e => e.SubcategoriaId).HasColumnName("subcategoria_id");
            entity.Property(e => e.Codigo).HasColumnName("codigo").HasMaxLength(50).IsRequired();
            entity.Property(e => e.Nombre).HasColumnName("nombre").HasMaxLength(150).IsRequired();
            entity.Property(e => e.Descripcion).HasColumnName("descripcion");
            entity.Property(e => e.PrecioUnitario).HasColumnName("precio_unitario").HasColumnType("decimal(12,2)").IsRequired();
            entity.Property(e => e.Estado).HasColumnName("estado");
        });

        // Proveedor
        modelBuilder.Entity<Proveedor>(entity =>
        {
            entity.ToTable("Proveedor");
            entity.HasKey(e => e.ProveedorId);
            entity.Property(e => e.ProveedorId).HasColumnName("proveedor_id");
            entity.Property(e => e.UsuarioId).HasColumnName("usuario_id");
            entity.Property(e => e.Contacto).HasColumnName("contacto").HasMaxLength(150);
        });

        // Rol
        modelBuilder.Entity<Rol>(entity =>
        {
            entity.ToTable("Rol");
            entity.HasKey(e => e.RolId);
            entity.Property(e => e.RolId).HasColumnName("rol_id");
            entity.Property(e => e.Nombre).HasColumnName("nombre").HasMaxLength(50).IsRequired();
        });

        // Subcategoria
        modelBuilder.Entity<Subcategoria>(entity =>
        {
            entity.ToTable("Subcategoria");
            entity.HasKey(e => e.SubcategoriaId);
            entity.Property(e => e.SubcategoriaId).HasColumnName("subcategoria_id");
            entity.Property(e => e.CategoriaId).HasColumnName("categoria_id");
            entity.Property(e => e.Nombre).HasColumnName("nombre").HasMaxLength(100).IsRequired();
            entity.Property(e => e.Estado).HasColumnName("estado");
            entity.HasIndex(e => new { e.CategoriaId, e.Nombre }).IsUnique();
        });

        // Cliente
        modelBuilder.Entity<Cliente>(entity =>
        {
            entity.ToTable("Cliente");
            entity.HasKey(e => e.ClienteId);
            entity.Property(e => e.ClienteId).HasColumnName("cliente_id");
            entity.Property(e => e.UsuarioId).HasColumnName("usuario_id");
            entity.Property(e => e.Contacto).HasColumnName("contacto").HasMaxLength(150);
            entity.Property(e => e.Direccion).HasColumnName("direccion").HasMaxLength(150);
            entity.Property(e => e.Sucursal).HasColumnName("sucursal").HasMaxLength(150);
        });

        // Inventario
        modelBuilder.Entity<Inventario>(entity =>
        {
            entity.ToTable("Inventario");
            entity.HasKey(e => e.InventarioId);
            entity.Property(e => e.InventarioId).HasColumnName("inventario_id");
            entity.Property(e => e.ProductoId).HasColumnName("producto_id");
            entity.Property(e => e.Ubicacion).HasColumnName("ubicacion").HasMaxLength(100).IsRequired();
            entity.Property(e => e.Cantidad).HasColumnName("cantidad");
            entity.Property(e => e.PuntoReorden).HasColumnName("punto_reorden");
            entity.Property(e => e.Estado).HasColumnName("estado");
            entity.HasIndex(e => new { e.ProductoId, e.Ubicacion }).IsUnique();
        });

        // OrdenReabastecimiento
        modelBuilder.Entity<OrdenReabastecimiento>(entity =>
        {
            entity.ToTable("OrdenReabastecimiento");
            entity.HasKey(e => e.OrdenId);
            entity.Property(e => e.OrdenId).HasColumnName("orden_id");
            entity.Property(e => e.ProveedorId).HasColumnName("proveedor_id");
            entity.Property(e => e.FechaCreacion).HasColumnName("fecha_creacion");
            entity.Property(e => e.Estado).HasColumnName("estado").HasMaxLength(50).IsRequired();
        });

        modelBuilder.Entity<OrdenReabastecimientoProducto>(entity =>
        {
            entity.ToTable("OrdenReabastecimiento_Producto");
            entity.HasKey(e => new { e.OrdenId, e.ProductoId });
            entity.Property(e => e.OrdenId).HasColumnName("orden_id");
            entity.Property(e => e.ProductoId).HasColumnName("producto_id");
            entity.Property(e => e.CantidadPedida).HasColumnName("cantidad_pedida");
            entity.Property(e => e.PrecioCompraUnitario).HasColumnName("precio_compra_unitario").HasColumnType("decimal(12,2)");
            entity.Property(e => e.Estado).HasColumnName("estado");
        });

        modelBuilder.Entity<Movimiento>(entity =>
        {
            entity.ToTable("Movimiento");
            entity.HasKey(e => e.MovimientoId);
            entity.Property(e => e.MovimientoId).HasColumnName("movimiento_id");
            entity.Property(e => e.InventarioId).HasColumnName("inventario_id");
            entity.Property(e => e.OrdenId).HasColumnName("orden_id");
            entity.Property(e => e.UsuarioId).HasColumnName("usuario_id");
            entity.Property(e => e.ProveedorId).HasColumnName("proveedor_id");
            entity.Property(e => e.ClienteId).HasColumnName("cliente_id");
            entity.Property(e => e.Tipo).HasColumnName("tipo").HasMaxLength(10);
            entity.Property(e => e.Cantidad).HasColumnName("cantidad");
            entity.Property(e => e.FechaMovimiento).HasColumnName("fecha_movimiento");
            entity.Property(e => e.Lote).HasColumnName("lote").HasMaxLength(100);
            entity.Property(e => e.Estado).HasColumnName("estado");
        });

        modelBuilder.Entity<Reporte>(entity =>
        {
            entity.ToTable("Reporte");
            entity.HasKey(e => e.ReporteId);
            entity.Property(e => e.ReporteId).HasColumnName("reporte_id");
            entity.Property(e => e.Titulo).HasColumnName("titulo").HasMaxLength(200);
            entity.Property(e => e.Descripcion).HasColumnName("descripcion");
            entity.Property(e => e.EvidenciaJson).HasColumnName("evidencia").HasColumnType("json");
            entity.Property(e => e.FechaCreado).HasColumnName("fecha_creado");
            entity.Property(e => e.TipoReporte).HasColumnName("tipo_reporte").HasMaxLength(50);
            entity.Property(e => e.Estado).HasColumnName("estado");
        });

        modelBuilder.Entity<ReporteUsuario>(entity =>
        {
            entity.ToTable("Reporte_Usuario");
            entity.HasKey(e => new { e.ReporteId, e.UsuarioId });
            entity.Property(e => e.ReporteId).HasColumnName("reporte_id");
            entity.Property(e => e.UsuarioId).HasColumnName("usuario_id");
        });

        modelBuilder.Entity<ReporteMovimiento>(entity =>
        {
            entity.ToTable("Reporte_Movimiento");
            entity.HasKey(e => new { e.ReporteId, e.MovimientoId });
            entity.Property(e => e.ReporteId).HasColumnName("reporte_id");
            entity.Property(e => e.MovimientoId).HasColumnName("movimiento_id");
        });

        modelBuilder.Entity<Tarea>(entity =>
        {
            entity.ToTable("Tarea");
            entity.HasKey(e => e.TareaId);
            entity.Property(e => e.TareaId).HasColumnName("tarea_id");
            entity.Property(e => e.Titulo).HasColumnName("titulo").HasMaxLength(200);
            entity.Property(e => e.Descripcion).HasColumnName("descripcion");
            entity.Property(e => e.AsignadoA).HasColumnName("asignado_a");
            entity.Property(e => e.FechaCreacion).HasColumnName("fecha_creacion");
            entity.Property(e => e.FechaFin).HasColumnName("fecha_fin");
            entity.Property(e => e.Estado).HasColumnName("estado");
        });

        modelBuilder.Entity<ReporteTarea>(entity =>
        {
            entity.ToTable("Reporte_Tarea");
            entity.HasKey(e => new { e.ReporteId, e.TareaId });
            entity.Property(e => e.ReporteId).HasColumnName("reporte_id");
            entity.Property(e => e.TareaId).HasColumnName("tarea_id");
        });

        modelBuilder.Entity<TareaProducto>(entity =>
        {
            entity.ToTable("Tarea_Producto");
            entity.HasKey(e => new { e.TareaId, e.ProductoId });
            entity.Property(e => e.TareaId).HasColumnName("tarea_id");
            entity.Property(e => e.ProductoId).HasColumnName("producto_id");
            entity.Property(e => e.Cantidad).HasColumnName("cantidad");
            entity.Property(e => e.Estado).HasColumnName("estado");
        });
    }
}
