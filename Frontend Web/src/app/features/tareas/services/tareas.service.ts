import { Injectable } from '@angular/core';
import { Observable, map } from 'rxjs';
import { HttpService } from '@core/services/http.service';
import { config } from '@environments/environment';

import { PagedResponse } from '@core/models/pagination.models';
import {
  Tarea,
  CreateTareaRequest,
  UpdateTareaRequest,
  TareaFilters,
} from '../models/tarea.models';

// Response del backend
interface BackendTarea {
  tareaId: number;
  titulo: string;
  descripcion?: string;
  asignadoA?: number;
  usuarioNombre?: string;
  fechaCreacion: string;
  fechaFin?: string;
  estado: boolean;
}

@Injectable({
  providedIn: 'root',
})
export class TareaService {
  private readonly apiUrl = `${config.api.baseUrl}/api/tareas`;

  constructor(private httpService: HttpService) {}

  /**
   * Mapea una tarea del backend al formato del frontend
   */
  private mapBackendToFrontend(backend: BackendTarea): Tarea {
    return {
      tareaId: backend.tareaId,
      titulo: backend.titulo,
      descripcion: backend.descripcion,
      asignadoA: backend.asignadoA,
      usuarioNombre: backend.usuarioNombre,
      fechaCreacion: new Date(backend.fechaCreacion),
      fechaFin: backend.fechaFin ? new Date(backend.fechaFin) : undefined,
      estado: backend.estado,
    };
  }

  /**
   * Obtiene la lista de tareas con filtros opcionales
   */
  getTareas(
    page: number,
    limit: number,
    filters?: TareaFilters
  ): Observable<PagedResponse<Tarea>> {
    const params: any = {
      pageNumber: page,
      pageSize: limit
    };

    if (filters?.estado !== undefined) {
      params.estado = filters.estado;
    }
    if (filters?.asignadoA) {
      params.asignadoA = filters.asignadoA;
    }

    return this.httpService.get<PagedResponse<BackendTarea>>(this.apiUrl, params).pipe(
      map(paged => {
        // Assume backend returns PagedResponse
        const mappedData = paged.data.map(t => this.mapBackendToFrontend(t));

        // Local filtering workaround if backend doesn't support filtering
        let filteredData = mappedData;

        // Note: estado and asignadoA are added to params, so backend should handle them.
        // If not, we keep local filtering.
        // Also search is not in params above, let's add it or filter locally.
        
        if (filters?.search) {
           // If search is supported by backend: params.search = filters.search;
           // Assuming it is not fully supported or we want to be safe:
           const searchTerm = filters.search.toLowerCase();
           filteredData = filteredData.filter(
            t =>
              t.titulo.toLowerCase().includes(searchTerm) ||
              (t.descripcion || '').toLowerCase().includes(searchTerm)
          );
        }

        // Double check local filtering if backend ignores params
        if (filters?.estado !== undefined) {
          filteredData = filteredData.filter(t => t.estado === filters.estado);
        }
        if (filters?.asignadoA) {
          filteredData = filteredData.filter(t => t.asignadoA === filters.asignadoA);
        }

        return {
          ...paged,
          data: filteredData
        };
      })
    );
  }

  /**
   * Obtiene una tarea por ID
   */
  getTareaById(id: number): Observable<Tarea> {
    return this.httpService
      .get<BackendTarea>(`${this.apiUrl}/${id}`)
      .pipe(map(t => this.mapBackendToFrontend(t)));
  }

  /**
   * Crea una nueva tarea
   */
  createTarea(tarea: CreateTareaRequest): Observable<Tarea> {
    return this.httpService
      .post<BackendTarea>(this.apiUrl, tarea)
      .pipe(map(t => this.mapBackendToFrontend(t)));
  }

  /**
   * Actualiza una tarea existente
   */
  updateTarea(id: number, tarea: UpdateTareaRequest): Observable<void> {
    return this.httpService.put<void>(`${this.apiUrl}/${id}`, tarea);
  }

  /**
   * Elimina una tarea
   */
  deleteTarea(id: number): Observable<void> {
    return this.httpService.delete<void>(`${this.apiUrl}/${id}`);
  }

  /**
   * Activa una tarea
   */
  activateTarea(id: number): Observable<void> {
    return this.httpService.post<void>(`${this.apiUrl}/${id}/activate`, {});
  }

  /**
   * Desactiva una tarea
   */
  deactivateTarea(id: number): Observable<void> {
    return this.httpService.post<void>(`${this.apiUrl}/${id}/deactivate`, {});
  }
}
