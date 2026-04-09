// Modelo principal de Proveedor - Compatible con frontend actual
export interface Proveedor {
  proveedor_id: number; // Mapeado de proveed orId del backend
  nombre: string; // Del usuario anidado
  contacto?: string;
  telefono?: string; // Del usuario anidado
  email?: string; // Del usuario anidado
  activo?: boolean;
  // Campos internos del backend
  proveedorId?: number;
  usuarioId?: number;
  usuario?: {
    usuarioId: number;
    rolId: number;
    nombre: string;
    cedula: number;
    email: string;
    telefono: string;
    estado: boolean;
  };
}

// Request para crear un nuevo proveedor
export interface CreateProveedorRequest {
  usuarioId?: number;
  contacto?: string;
}

// Request para actualizar un proveedor existente
export interface UpdateProveedorRequest {
  usuarioId?: number;
  contacto?: string;
}

// Filtros para búsqueda de proveedores
export interface ProveedorFilters {
  search?: string;
  pageNumber?: number;
  pageSize?: number;
  activo?: boolean;
}

