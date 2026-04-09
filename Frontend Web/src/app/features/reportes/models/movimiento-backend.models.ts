export interface MovimientoResponse {
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
}

export interface InventarioResponse {
  inventarioId: number;
  productoId: number;
  ubicacion: string;
  cantidad: number;
  puntoReorden: number;
  estado: boolean;
}

export interface ProductoResponse {
  productoId: number;
  subcategoriaId: number;
  codigo: string;
  nombre: string;
  descripcion?: string;
  precioUnitario: number;
  estado: boolean;
}

export interface UsuarioResponse {
  usuarioId: number;
  rolId: number;
  nombre: string;
  cedula: string;
  email: string;
  telefono?: string;
  estado: boolean;
}

export interface ProveedorResponse {
  proveedorId: number;
  usuarioId?: number;
  contacto?: string;
  usuario?: UsuarioResponse;
}

export interface ClienteResponse {
  clienteId: number;
  usuarioId?: number;
  contacto?: string;
  direccion?: string;
  sucursal?: string;
  usuario?: UsuarioResponse;
}
