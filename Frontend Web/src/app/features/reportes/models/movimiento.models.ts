// Modelo del backend (.NET)
export interface BackendMovimiento {
  movimientoId: number;
  inventarioId: number;
  ordenId?: number;
  usuarioId: number;
  proveedorId?: number;
  clienteId?: number;
  tipo: string;
  cantidad: number;
  fechaMovimiento: string;
  lote?: string;
  estado: boolean;
  productoNombre?: string;
  productoCodigo?: string;
  usuarioNombre?: string;
  proveedorNombre?: string;
  clienteNombre?: string;
}

// Modelo del frontend optimizado para UI
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
  inventarioId: number;
  usuarioId: number;
  lote?: string;
  estado: boolean;
  ordenId?: number;
  proveedorId?: number;
  clienteId?: number;
}

export interface MovimientoFilters {
  tipo?: 'entrada' | 'salida';
  inventarioId?: number;
  fechaInicio?: string;
  fechaFin?: string;
  search?: string;
  pageNumber?: number;
  pageSize?: number;
}

export interface MovimientosResponse {
  data: Movimiento[];
  total: number;
}
