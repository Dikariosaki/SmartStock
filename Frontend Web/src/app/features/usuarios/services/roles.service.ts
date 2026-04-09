import { Injectable } from '@angular/core';
import { Observable, of } from 'rxjs';
import { map, catchError, shareReplay } from 'rxjs/operators';
import { HttpService } from '@core/services/http.service';
import { config } from '@environments/environment';

import { PagedResponse } from '@core/models/pagination.models';

// Response del backend
interface BackendRol {
  rolId: number;
  nombre: string;
}

export interface Rol {
  rolId: number;
  nombre: string;
}

@Injectable({
  providedIn: 'root',
})
export class RolesService {
  private readonly apiUrl = config.api.endpoints.roles;
  private rolesCache$?: Observable<Rol[]>;

  constructor(private httpService: HttpService) {}

  /**
   * Obtiene todos los roles disponibles
   * Usa caché para evitar llamadas repetidas
   */
  getRoles(): Observable<Rol[]> {
    if (!this.rolesCache$) {
      // Solicitamos una página grande para asegurar traer todos los roles
      const params = { pageNumber: 1, pageSize: 100 };
      
      this.rolesCache$ = this.httpService.get<PagedResponse<BackendRol>>(this.apiUrl, params).pipe(
        map(response => response.data.map(r => this.mapBackendToFrontend(r))),
        shareReplay(1), // Cachea el resultado
        catchError(error => {
          console.error('Error al cargar roles:', error);
          // En caso de error, retornar lista vacía
          return of([]);
        })
      );
    }
    return this.rolesCache$;
  }

  /**
   * Invalida el caché de roles
   * Útil después de crear/actualizar/eliminar un rol
   */
  invalidateCache(): void {
    this.rolesCache$ = undefined;
  }

  private mapBackendToFrontend(backendRol: BackendRol): Rol {
    return {
      rolId: backendRol.rolId,
      nombre: backendRol.nombre,
    };
  }
}
