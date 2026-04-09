import { Injectable } from '@angular/core';
import { Observable, map, forkJoin } from 'rxjs';
import { HttpService } from '@core/services/http.service';
import { config } from '@environments/environment';
import { RolesService } from './roles.service';

import { PagedResponse } from '@core/models/pagination.models';
import {
  Usuario,
  CreateUsuarioRequest,
  UpdateUsuarioRequest,
  UsuarioFilters,
} from '../models/usuario.models';

// Response del backend
interface BackendUsuario {
  usuarioId: number;
  rolId: number;
  nombre: string;
  cedula: number;
  email: string;
  telefono: string;
  estado: boolean;
}

@Injectable({
  providedIn: 'root',
})
export class UsuarioService {
  private readonly apiUrl = config.api.endpoints.usuarios;

  constructor(
    private httpService: HttpService,
    private rolesService: RolesService
  ) {}

  /**
   * Mapea un usuario del backend al formato del frontend
   */
  private mapBackendToFrontend(
    backendUsuario: BackendUsuario,
    rolNombre?: string
  ): Usuario {
    return {
      usuario_id: backendUsuario.usuarioId,
      nombre: backendUsuario.nombre,
      identificacion: backendUsuario.cedula.toString(),
      correo: backendUsuario.email,
      telefono: backendUsuario.telefono,
      activo: backendUsuario.estado,
      rolNombre: rolNombre,
      rolId: backendUsuario.rolId,
      passwordHash: (backendUsuario as any).passwordHash, 
    };
  }

  getUsuarios(
    page: number,
    limit: number,
    filters?: UsuarioFilters
  ): Observable<PagedResponse<Usuario>> {
    const params: any = {
      pageNumber: page,
      pageSize: limit
    };

    if (filters?.estado !== undefined && filters?.estado !== null) {
      params.estado = filters.estado;
    }

    if (filters?.roleNames && filters.roleNames.length > 0) {
      filters.roleNames.forEach((role, index) => {
        params[`roleNames[${index}]`] = role;
      });
    }

    return forkJoin({
      paged: this.httpService.get<PagedResponse<BackendUsuario> | BackendUsuario[]>(this.apiUrl, params),
      roles: this.rolesService.getRoles(),
    }).pipe(
      map(({ paged, roles }) => {
        // Handle paged response safely (array vs PagedResponse)
        let data: BackendUsuario[] = [];
        let totalCount = 0;
        let pageNumber = page;
        let pageSize = limit;
        let totalPages = 0;

        if (Array.isArray(paged)) {
            data = paged;
            totalCount = paged.length;
            totalPages = Math.ceil(totalCount / pageSize);
        } else {
            data = paged.data || [];
            totalCount = paged.totalCount || 0;
            pageNumber = paged.pageNumber || pageNumber;
            pageSize = paged.pageSize || pageSize;
            totalPages = paged.totalPages || Math.ceil(totalCount / pageSize);
        }

        // Mapear usuarios con nombre de rol
        const mappedData = data.map(u => {
          const rol = roles.find(r => r.rolId === u.rolId);
          return this.mapBackendToFrontend(u, rol?.nombre);
        });

        let filteredData = mappedData;
        

        if (filters?.search) {
          const term = filters.search.toLowerCase();
          filteredData = filteredData.filter(
            u =>
              u.nombre.toLowerCase().includes(term) ||
              u.identificacion.includes(term) ||
              u.correo.toLowerCase().includes(term) ||
              (u.rolNombre && u.rolNombre.toLowerCase().includes(term))
          );
        }

        return {
           data: filteredData,
           totalCount: totalCount,
           pageNumber: pageNumber,
           pageSize: pageSize,
           totalPages: totalPages,
           hasNextPage: pageNumber < totalPages,
           hasPreviousPage: pageNumber > 1
        };
      })
    );
  }

  createUsuario(req: CreateUsuarioRequest): Observable<Usuario> {
    return this.httpService
      .post<BackendUsuario>(this.apiUrl, req)
      .pipe(map(u => this.mapBackendToFrontend(u)));
  }

  updateUsuario(id: number, req: UpdateUsuarioRequest): Observable<void> {
    // Sanitizar el request para evitar enviar campos sensibles o vacíos por error
    const sanitizedReq = { ...req };
    delete (sanitizedReq as any).password;
    delete (sanitizedReq as any).passwordHash;

    return this.httpService.put<void>(`${this.apiUrl}/${id}`, sanitizedReq);
  }

  // Versión que devuelve observable con el objeto mapeado
  updateUsuarioObservable(id: number, req: UpdateUsuarioRequest): Observable<Usuario> {
    const sanitizedReq = { ...req };
    delete (sanitizedReq as any).password;
    
    return this.httpService.put<void>(`${this.apiUrl}/${id}`, sanitizedReq).pipe(
      map(() => {
        const mappedUsuario: Usuario = {
          usuario_id: id,
          nombre: req.nombre || '',
          identificacion: req.cedula?.toString() || req.identificacion || '',
          correo: req.email || req.correo || '',
          telefono: req.telefono || '',
          activo: req.estado ?? req.activo ?? true,
        };
        return mappedUsuario;
      })
    );
  }

  getUsuarioById(id: number): Observable<Usuario> {
    return forkJoin({
      backendUsuario: this.httpService.get<BackendUsuario>(`${this.apiUrl}/${id}`),
      roles: this.rolesService.getRoles(),
    }).pipe(
      map(({ backendUsuario, roles }) => {
        const rol = roles.find(r => r.rolId === backendUsuario.rolId);
        return this.mapBackendToFrontend(backendUsuario, rol?.nombre);
      })
    );
  }

  deleteUsuario(id: number): Observable<void> {
    return this.httpService.delete<void>(`${this.apiUrl}/${id}`);
  }

  deactivateUsuario(id: number): Observable<Usuario> {
    return this.httpService
      .post<void>(`${this.apiUrl}/${id}/deactivate`, {})
      .pipe(
        map(() => ({
          usuario_id: id,
          nombre: '',
          identificacion: '',
          correo: '',
          telefono: '',
          activo: false,
        }))
      );
  }

  activateUsuario(id: number): Observable<Usuario> {
    return this.httpService
      .post<void>(`${this.apiUrl}/${id}/activate`, {})
      .pipe(
        map(() => ({
          usuario_id: id,
          nombre: '',
          identificacion: '',
          correo: '',
          telefono: '',
          activo: true,
        }))
      );
  }
}
