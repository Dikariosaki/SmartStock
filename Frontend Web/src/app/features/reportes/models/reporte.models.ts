export interface ReporteMovimiento {
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
