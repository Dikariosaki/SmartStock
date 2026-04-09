// Modelo principal de Cliente - Compatible con frontend actual
export interface Cliente {
  cliente_id: number; // Mapeado de clienteId del backend
  nombre: string; // Del usuario anidado
  contacto?: string;
  telefono?: string; // Del usuario anidado
  email?: string; // Del usuario anidado
  activo?: boolean;
  // Campos internos del backend
  clienteId?: number;
  usuarioId?: number;
  direccion?: string;
  sucursal?: string;
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

// Request para crear un nuevo cliente
export interface CreateClienteRequest {
  usuarioId?: number;
  contacto?: string;
  direccion?: string;
  sucursal?: string;
}

// Request para actualizar un cliente existente
export interface UpdateClienteRequest {
  usuarioId?: number;
  contacto?: string;
  direccion?: string;
  sucursal?: string;
}

// Filtros para búsqueda de clientes
export interface ClienteFilters {
  search?: string;
  pageNumber?: number;
  pageSize?: number;
  activo?: boolean;
}


// Estados para el manejo de la UI
export interface ClienteState {
  clientes: Cliente[];
  loading: boolean;
  error: string | null;
  selectedCliente: Cliente | null;
}

// Tipos para las acciones de CRUD
export type ClienteAction =
  | 'create'
  | 'edit'
  | 'delete'
  | 'deactivate'
  | 'view';

// Interface para el modal state
export interface ClienteModalState {
  isOpen: boolean;
  action: ClienteAction;
  cliente?: Cliente;
}
