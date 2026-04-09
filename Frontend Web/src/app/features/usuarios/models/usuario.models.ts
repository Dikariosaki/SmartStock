export interface Rol {
  rolId: number;
  nombre: string;
}

export interface Usuario {
  usuario_id: number;
  nombre: string;
  identificacion: string;
  correo: string;
  telefono: string;
  activo?: boolean;
  rolNombre?: string;
  rolId?: number;
  passwordHash?: string;
}

export interface CreateUsuarioRequest {
  rolId: number;
  nombre: string;
  cedula: number;
  email: string;
  password: string;
  telefono?: string;
  estado?: boolean;
  // Aliases para mantener compatibilidad
  identificacion?: string;
  correo?: string;
}

export interface UpdateUsuarioRequest {
  rolId?: number;
  nombre?: string;
  cedula?: number;
  email?: string;
  password?: string;
  telefono?: string;
  estado?: boolean;
  // Aliases para mantener compatibilidad
  identificacion?: string;
  correo?: string;
  activo?: boolean;
}

export interface UsuarioFilters {
  search?: string;
  estado?: boolean;
  roleNames?: string[];
  pageNumber?: number;
  pageSize?: number;
}

