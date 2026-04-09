// Modelo principal de Categoria (alineado con backend)
export interface Categoria {
  categoriaId: number;
  nombre: string;
  estado: boolean;
}

// Request para crear una nueva categoría
export interface CreateCategoriaRequest {
  nombre: string;
  estado?: boolean;
}

// Request para actualizar una categoría existente
export interface UpdateCategoriaRequest {
  nombre: string;
  estado: boolean;
}

// Filtros para búsqueda de categorías
export interface CategoriaFilters {
  nombre?: string;
  search?: string;
  estado?: boolean;
  pageNumber?: number;
  pageSize?: number;
}


// Estados para el manejo de la UI
export interface CategoriaState {
  categorias: Categoria[];
  loading: boolean;
  error: string | null;
  selectedCategoria: Categoria | null;
}

// Tipos para las acciones de CRUD
export type CategoriaAction =
  | 'create'
  | 'edit'
  | 'delete'
  | 'deactivate'
  | 'view';

// Estado del modal
export interface CategoriaModalState {
  show: boolean;
  mode: CategoriaAction;
  categoria: Categoria | null;
}
