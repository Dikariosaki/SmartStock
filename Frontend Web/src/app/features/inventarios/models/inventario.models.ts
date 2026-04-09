export interface Producto {
  productoId: number;
  codigo: string;
  nombre: string;
  subcategoriaId?: number;
}

export interface Inventario {
  inventarioId: number;
  productoId: number;
  ubicacion: string;
  cantidad: number;
  puntoReorden: number;
  estado: boolean;
  // Info del producto (opcional, para mostrar en la lista)
  producto?: Producto;
}

export interface CreateInventarioRequest {
  productoId: number;
  ubicacion: string;
  cantidad: number;
  puntoReorden: number;
  estado?: boolean;
}

export interface UpdateInventarioRequest {
  productoId: number;
  ubicacion: string;
  cantidad: number;
  puntoReorden: number;
  estado: boolean;
}

export interface InventarioFilters {
  search?: string;
  productoId?: number;
  ubicacion?: string;
  bajoPuntoReorden?: boolean; // Para filtrar productos bajo stock mínimo
  pageNumber?: number;
  pageSize?: number;
  estado?: boolean;
}

