export interface Producto {
  producto_id: number;
  nombre: string;
  categoria: string;
  subcategoria: string;
  cantidad: number;
  cliente: string;
  proveedor: string;
  precio: number; // La segunda columna de números de tu imagen
  activo?: boolean;
}

export interface CreateProductoRequest {
  nombre: string;
  categoria: string;
  subcategoria: string;
  cantidad: number;
  cliente: string;
  proveedor: string;
  precio: number;
  activo?: boolean;
}

export interface UpdateProductoRequest {
  nombre?: string;
  categoria?: string;
  subcategoria?: string;
  cantidad?: number;
  cliente?: string;
  proveedor?: string;
  precio?: number;
  activo?: boolean;
}

export interface ProductoFilters {
  search?: string;
  page?: number;
  limit?: number;
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
}
