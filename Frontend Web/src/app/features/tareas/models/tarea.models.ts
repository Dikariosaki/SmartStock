// Modelo principal de Tarea (alineado con backend TareaResponse)
export interface Tarea {
  tareaId: number;
  titulo: string;
  descripcion?: string;
  asignadoA?: number;
  usuarioNombre?: string;
  fechaCreacion: Date;
  fechaFin?: Date;
  estado: boolean;
}

// Request para crear una nueva tarea
export interface CreateTareaRequest {
  titulo: string;
  descripcion?: string;
  asignadoA?: number;
  estado?: boolean;
}

// Request para actualizar una tarea existente
export interface UpdateTareaRequest {
  titulo: string;
  descripcion?: string;
  asignadoA?: number;
  fechaFin?: Date;
  estado: boolean;
}

// Filtros para búsqueda de tareas
export interface TareaFilters {
  search?: string;
  asignadoA?: number;
  estado?: boolean;
  pageNumber?: number;
  pageSize?: number;
}

