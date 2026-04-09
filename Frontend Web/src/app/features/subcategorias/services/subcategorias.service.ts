import { Injectable } from '@angular/core';
import { Observable, map } from 'rxjs';
import { HttpService } from '@core/services/http.service';
import { config } from '@environments/environment';

import { PagedResponse } from '@core/models/pagination.models';
import {
  Subcategoria,
  CreateSubcategoriaRequest,
  UpdateSubcategoriaRequest,
  SubcategoriaFilters,
} from '../models/subcategoria.models';

@Injectable({
  providedIn: 'root',
})
export class SubcategoriaService {
  private readonly apiUrl = `${config.api.baseUrl}/api/subcategorias`;

  constructor(private httpService: HttpService) {}

  private mapBackendToFrontend(backend: any): Subcategoria {
    return {
      subcategoriaId: backend.subcategoriaId,
      categoriaId: backend.categoriaId,
      categoriaNombre: backend.categoriaNombre || '',
      nombre: backend.nombre,
      estado: backend.estado,
    };
  }

  getSubcategorias(
    page: number,
    limit: number,
    filters?: SubcategoriaFilters
  ): Observable<PagedResponse<Subcategoria>> {
    const params: any = {
      pageNumber: page,
      pageSize: limit
    };

    if (filters?.estado !== undefined) {
      params.estado = filters.estado;
    }
    if (filters?.categoriaId) {
      params.categoriaId = filters.categoriaId;
    }

    return this.httpService.get<PagedResponse<Subcategoria>>(this.apiUrl, params).pipe(
      map(paged => {
        // Assume backend returns PagedResponse
        const mappedData = paged.data.map(s => this.mapBackendToFrontend(s));

        // Local filtering workaround
        let filteredData = mappedData;
        if (filters?.search) {
          const searchTerm = filters.search.toLowerCase();
          filteredData = filteredData.filter(s =>
            s.nombre.toLowerCase().includes(searchTerm)
          );
        }

        // Note: categoriaId filtering should ideally be handled by backend.
        // If we added it to params above, backend should filter.
        // If backend ignores it, we filter locally here too.
        if (filters?.categoriaId) {
             filteredData = filteredData.filter(s => s.categoriaId === filters.categoriaId);
        }

        return {
          ...paged,
          data: filteredData
        };
      })
    );
  }

  /**
   * Obtiene una subcategoría por ID
   */
  getSubcategoriaById(id: number): Observable<Subcategoria> {
    return this.httpService
      .get<Subcategoria>(`${this.apiUrl}/${id}`)
      .pipe(map(s => this.mapBackendToFrontend(s)));
  }

  /**
   * Obtiene subcategorías por categoría
   */
  getSubcategoriasByCategoria(categoriaId: number): Observable<Subcategoria[]> {
    return this.httpService
      .get<Subcategoria[]>(`${this.apiUrl}/categoria/${categoriaId}`)
      .pipe(map(subs => subs.map(s => this.mapBackendToFrontend(s))));
  }

  /**
   * Crea una nueva subcategoría
   */
  createSubcategoria(
    subcategoria: CreateSubcategoriaRequest
  ): Observable<Subcategoria> {
    return this.httpService
      .post<Subcategoria>(this.apiUrl, subcategoria)
      .pipe(map(s => this.mapBackendToFrontend(s)));
  }

  /**
   * Actualiza una subcategoría existente
   */
  updateSubcategoria(
    id: number,
    subcategoria: UpdateSubcategoriaRequest
  ): Observable<void> {
    return this.httpService.put<void>(`${this.apiUrl}/${id}`, subcategoria);
  }

  activarSubcategoria(id: number): Observable<void> {
    return this.httpService.put<void>(`${this.apiUrl}/${id}/activar`, {});
  }

  desactivarSubcategoria(id: number): Observable<void> {
    return this.httpService.put<void>(`${this.apiUrl}/${id}/desactivar`, {});
  }

  /**
   * Elimina una subcategoría
   */
  deleteSubcategoria(id: number): Observable<void> {
    return this.httpService.delete<void>(`${this.apiUrl}/${id}`);
  }

  /**
   * Activa una subcategoría
   */
  activateSubcategoria(id: number): Observable<void> {
    return this.httpService.post<void>(`${this.apiUrl}/${id}/activate`, {});
  }

  /**
   * Desactiva una subcategoría
   */
  deactivateSubcategoria(id: number): Observable<void> {
    return this.httpService.post<void>(`${this.apiUrl}/${id}/deactivate`, {});
  }
}
