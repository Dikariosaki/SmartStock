// Modelo principal de Subcategoria (alineado con backend)
export interface Subcategoria {
  subcategoriaId: number;
  categoriaId: number;
  categoriaNombre: string;
  nombre: string;
  estado: boolean;
}

// Request para crear una nueva subcategoría
export interface CreateSubcategoriaRequest {
  categoriaId: number;
  nombre: string;
  estado?: boolean;
}

// Request para actualizar una subcategoría existente
export interface UpdateSubcategoriaRequest {
  categoriaId: number;
  nombre: string;
  estado: boolean;
}

// Filtros para búsqueda de subcategorías
export interface SubcategoriaFilters {
  nombre?: string;
  search?: string;
  categoriaId?: number;
  estado?: boolean;
  pageNumber?: number;
  pageSize?: number;
}


// Estados para el manejo de la UI (opcional, pero buena práctica)
export interface SubcategoriaState {
  subcategorias: Subcategoria[];
  loading: boolean;
  error: string | null;
  selectedSubcategoria: Subcategoria | null;
}

// Tipos para las acciones de CRUD
export type SubcategoriaAction =
  | 'create'
  | 'edit'
  | 'delete'
  | 'deactivate'
  | 'view';

// Estado del modal
export interface SubcategoriaModalState {
  show: boolean;
  mode: SubcategoriaAction;
  subcategoria: Subcategoria | null;
}
