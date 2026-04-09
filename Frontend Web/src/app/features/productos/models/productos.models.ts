export interface Subcategoria {
  subcategoriaId: number;
  nombre: string;
  categoriaId?: number;
  categoriaNombre?: string;
  estado?: boolean;
}

export interface Producto {
  productoId: number; // Antes era producto_id
  subcategoriaId: number; // Antes era un string nombre
  codigo: string; // ¡NUEVO! Obligatorio
  nombre: string;
  descripcion: string; // ¡NUEVO!
  precioUnitario: number; // Antes era precio
  estado: boolean; // Antes era activo
  subcategoria?: Subcategoria; // Info completa de la subcategoría (opcional, para mostrar nombre)
}

export interface CreateProductoRequest {
  subcategoriaId: number;
  codigo?: string; // Opcional - se auto-genera en backend
  nombre: string;
  descripcion?: string;
  precioUnitario: number;
  estado?: boolean;
}

export interface UpdateProductoRequest {
  subcategoriaId?: number;
  codigo?: string;
  nombre?: string;
  descripcion?: string;
  precioUnitario?: number;
  estado?: boolean;
}

export interface ProductoFilters {
  search?: string;
  pageNumber?: number;
  pageSize?: number;
  estado?: boolean;
}

export interface ProductosListResponse {
  data: Producto[];
  meta: {
    total: number;
    page: number;
    limit: number;
  };
}

export interface Movimiento {
  id: number;
  productoId: number;
  productoNombre: string;
  cantidad: number;
  tipo: 'entrada' | 'salida';
  fecha: Date;
  asignadoA?: string;
  proveedor?: string;
  cliente?: string;
  direccion?: string;
  sucursal?: string;
  precio?: number;
}
