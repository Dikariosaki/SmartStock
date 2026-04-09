import { Injectable } from '@angular/core';
import { Observable, map, switchMap, catchError } from 'rxjs';
import { HttpService } from '@core/services/http.service';
import { config } from '@environments/environment';
import { PagedResponse } from '@core/models/pagination.models';
import {
  Proveedor,
  CreateProveedorRequest,
  UpdateProveedorRequest,
  ProveedorFilters,
} from '../models/proveedor.models';
import { UsuarioService } from '@features/usuarios/services/usuarios.service';

// Response del backend
interface BackendProveedor {
  proveedorId: number;
  usuarioId?: number;
  contacto?: string;
  // Campos planos
  nombre?: string;
  email?: string;
  telefono?: string;
  estado?: boolean;
  // Campos anidados
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

@Injectable({
  providedIn: 'root',
})
export class ProveedorService {
  private readonly apiUrl = config.api.endpoints.proveedores;

  constructor(
    private httpService: HttpService,
    private usuarioService: UsuarioService
  ) {}

  /**
   * Mapea un proveedor del backend al formato del frontend
   */
  private mapBackendToFrontend(backendProveedor: BackendProveedor): Proveedor {
    return {
      proveedor_id: backendProveedor.proveedorId,
      proveedorId: backendProveedor.proveedorId,
      usuarioId: backendProveedor.usuarioId,
      nombre: backendProveedor.nombre || backendProveedor.usuario?.nombre || '',
      contacto: backendProveedor.contacto,
      telefono: backendProveedor.telefono || backendProveedor.usuario?.telefono || '-',
      email: backendProveedor.email || backendProveedor.usuario?.email || '-',
      activo: backendProveedor.estado ?? backendProveedor.usuario?.estado ?? true,
      usuario: backendProveedor.usuario,
    };
  }

  /**
   * Obtiene la lista de proveedores con filtros opcionales
   * Añade paginación y filtros locales
   */
  getProveedores(
    page: number,
    limit: number,
    filters?: ProveedorFilters
  ): Observable<PagedResponse<Proveedor>> {
    const params: any = {
      pageNumber: page,
      pageSize: limit
    };

    return this.httpService.get<PagedResponse<BackendProveedor> | BackendProveedor[]>(this.apiUrl, params).pipe(
      map(response => {
        let data: BackendProveedor[] = [];
        let totalCount = 0;
        let pageNumber = page;
        let pageSize = limit;
        let totalPages = 0;

        if (Array.isArray(response)) {
          data = response;
          totalCount = response.length;
          totalPages = Math.ceil(totalCount / pageSize);
        } else {
          data = response.data || [];
          totalCount = response.totalCount || 0;
          pageNumber = response.pageNumber || pageNumber;
          pageSize = response.pageSize || pageSize;
          totalPages = response.totalPages || Math.ceil(totalCount / pageSize);
        }

        const mapped = data.map(p => this.mapBackendToFrontend(p));

        // Aplicar filtros de búsqueda localmente (workaround)
        let filtered = mapped;
        if (filters?.search) {
          const searchTerm = filters.search.toLowerCase();
          filtered = mapped.filter(
            p =>
              p.nombre.toLowerCase().includes(searchTerm) ||
              (p.contacto ?? '').toLowerCase().includes(searchTerm) ||
              (p.email ?? '').toLowerCase().includes(searchTerm) ||
              (p.telefono ?? '').includes(searchTerm)
          );
        }

        return {
          data: filtered,
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

  /**
   * Obtiene un proveedor por su ID
   */
  getProveedorById(id: number): Observable<Proveedor> {
    return this.httpService
      .get<BackendProveedor>(`${this.apiUrl}/${id}`)
      .pipe(map(p => this.mapBackendToFrontend(p)));
  }

  /**
   * Obtiene proveedores por usuario ID
   */
  getProveedoresByUsuario(usuarioId: number): Observable<Proveedor[]> {
    return this.httpService
      .get<BackendProveedor[]>(`${this.apiUrl}/usuario/${usuarioId}`)
      .pipe(
        map(proveedores => proveedores.map(p => this.mapBackendToFrontend(p)))
      );
  }

  /**
   * Busca un proveedor por nombre
   */
  buscarProveedorPorNombre(nombre: string): Observable<Proveedor> {
    return this.httpService.get<BackendProveedor[]>(this.apiUrl).pipe(
      map(proveedores => {
        const found = proveedores.find(p =>
          p.usuario?.nombre.toLowerCase().includes(nombre.toLowerCase())
        );
        if (!found) {
          throw new Error('Proveedor no encontrado');
        }
        return this.mapBackendToFrontend(found);
      })
    );
  }

  /**
   * Crea un nuevo proveedor
   */
  createProveedor(proveedor: CreateProveedorRequest): Observable<Proveedor> {
    return this.httpService
      .post<BackendProveedor>(this.apiUrl, proveedor)
      .pipe(map(p => this.mapBackendToFrontend(p)));
  }

  /**
   * Crea un proveedor completo (Usuario + Proveedor) con rollback
   */
  createProveedorConUsuario(datos: {
    nombre: string;
    cedula: number;
    email: string;
    telefono?: string;
    contacto?: string;
  }): Observable<Proveedor> {
    // 1. Preparar datos del usuario
    const usuarioRequest = {
      rolId: 4, // Rol Proveedor
      nombre: datos.nombre,
      cedula: datos.cedula,
      email: datos.email,
      password: 'Proveedor123!', // Password por defecto
      telefono: datos.telefono || undefined,
      estado: true,
    };

    console.log('📤 Request a API Usuarios:', usuarioRequest);

    // 2. Crear Usuario
    return this.usuarioService.createUsuario(usuarioRequest).pipe(
      switchMap(usuario => {
        // 3. Preparar datos del proveedor
        const proveedorRequest: CreateProveedorRequest = {
          usuarioId: usuario.usuario_id,
          contacto: datos.contacto,
        };

        // 4. Crear Proveedor
        return this.createProveedor(proveedorRequest).pipe(
          catchError(error => {
            console.error(
              'Error al crear proveedor, revirtiendo usuario...',
              error
            );
            // 5. Rollback: Eliminar usuario si falla la creación del proveedor
            return this.usuarioService.deleteUsuario(usuario.usuario_id).pipe(
              switchMap(() => {
                throw error;
              }),
              catchError(() => {
                throw error;
              })
            );
          })
        );
      })
    );
  }

  /**
   * Actualiza un proveedor existente
   */
  updateProveedor(
    id: number,
    proveedor: UpdateProveedorRequest
  ): Observable<void> {
    return this.httpService.put<void>(`${this.apiUrl}/${id}`, proveedor);
  }

  /**
   * Elimina un proveedor y su usuario asociado
   */
  deleteProveedor(id: number): Observable<boolean> {
    // 1. Primero obtener el proveedor para saber el usuarioId
    return this.getProveedorById(id).pipe(
      switchMap(proveedor => {
        const usuarioId = proveedor.usuario?.usuarioId;
        // 2. Eliminar el proveedor
        return this.httpService.delete<void>(`${this.apiUrl}/${id}`).pipe(
          switchMap(() => {
            // 3. Si hay usuario asociado, eliminarlo también
            if (usuarioId) {
              console.log(
                `🗑️ Eliminando usuario ${usuarioId} asociado al proveedor`
              );
              return this.usuarioService.deleteUsuario(usuarioId).pipe(
                map(() => true),
                catchError(error => {
                  console.error('Error al eliminar usuario asociado:', error);
                  // Aún así retornamos true porque el proveedor ya fue eliminado
                  return [true];
                })
              );
            } else {
              // No hay usuario asociado, solo retornar true
              return [true];
            }
          })
        );
      }),
      catchError(error => {
        console.error('Error al eliminar proveedor:', error);
        throw error;
      })
    );
  }

  /**
   * Da de baja un proveedor (desactiva el usuario asociado)
   */
  deactivateProveedor(id: number): Observable<boolean> {
    // Primero obtener el proveedor para tener el usuarioId
    return this.getProveedorById(id).pipe(
      switchMap(proveedor => {
        if (!proveedor.usuarioId) {
          throw new Error('El proveedor no tiene un usuario asociado');
        }
        return this.usuarioService.deactivateUsuario(proveedor.usuarioId);
      }),
      map(() => true)
    );
  }
}
