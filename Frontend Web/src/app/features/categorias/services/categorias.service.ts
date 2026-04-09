import { Injectable } from '@angular/core';
import { Observable, map } from 'rxjs';
import { HttpService } from '@core/services/http.service';
import { config } from '@environments/environment';

import { PagedResponse } from '@core/models/pagination.models';
import {
  Categoria,
  CreateCategoriaRequest,
  UpdateCategoriaRequest,
  CategoriaFilters,
} from '../models/categoria.models';

// Response del backend
interface BackendCategoria {
  categoriaId: number;
  nombre: string;
  estado: boolean;
}

@Injectable({
  providedIn: 'root',
})
export class CategoriaService {
  private readonly apiUrl = `${config.api.baseUrl}/api/categorias`;

  constructor(private httpService: HttpService) {}

  /**
   * Mapea una categoría del backend al formato del frontend
   */
  private mapBackendToFrontend(backend: BackendCategoria): Categoria {
    return {
      categoriaId: backend.categoriaId,
      nombre: backend.nombre,
      estado: backend.estado,
    };
  }

  /**
   * Obtiene la lista de categorías con filtros opcionales
   */
  getCategorias(
    page: number,
    limit: number,
    filters?: CategoriaFilters
  ): Observable<PagedResponse<Categoria>> {
    const params: any = {
      pageNumber: page,
      pageSize: limit
    };

    if (filters?.estado !== undefined) {
      params.estado = filters.estado;
    }

    return this.httpService.get<PagedResponse<BackendCategoria>>(this.apiUrl, params).pipe(
      map(paged => {
        // If the backend returns an array (old behavior), we need to handle it.
        // But we assume backend is updated.
        // If paged is actually an array, we'd have runtime errors accessing .data
        // We can add a check if we are unsure, but let's stick to the plan.
        
        const mappedData = paged.data.map(c => this.mapBackendToFrontend(c));
        
        // Local filtering if needed (workaround)
        let filteredData = mappedData;
        if (filters?.search) {
           const term = filters.search.toLowerCase();
           filteredData = filteredData.filter(c => 
             c.nombre.toLowerCase().includes(term)
           );
        }

        return {
          ...paged,
          data: filteredData
        };
      })
    );
  }

  /**
   * Obtiene una categoría por ID
   */
  getCategoriaById(id: number): Observable<Categoria> {
    return this.httpService
      .get<BackendCategoria>(`${this.apiUrl}/${id}`)
      .pipe(map(c => this.mapBackendToFrontend(c)));
  }

  /**
   * Crea una nueva categoría
   */
  createCategoria(categoria: CreateCategoriaRequest): Observable<Categoria> {
    return this.httpService
      .post<BackendCategoria>(this.apiUrl, categoria)
      .pipe(map(c => this.mapBackendToFrontend(c)));
  }

  /**
   * Actualiza una categoría existente
   */
  updateCategoria(
    id: number,
    categoria: UpdateCategoriaRequest
  ): Observable<void> {
    return this.httpService.put<void>(`${this.apiUrl}/${id}`, categoria);
  }

  /**
   * Elimina una categoría
   */
  deleteCategoria(id: number): Observable<void> {
    return this.httpService.delete<void>(`${this.apiUrl}/${id}`);
  }

}
