import { Injectable } from '@angular/core';
import { Observable, map, switchMap, catchError } from 'rxjs';
import { HttpService } from '@core/services/http.service';
import { config } from '@environments/environment';
import { PagedResponse } from '@core/models/pagination.models';
import {
  Cliente,
  CreateClienteRequest,
  UpdateClienteRequest,
  ClienteFilters,
} from '@features/clientes/models/cliente.models';
import { UsuarioService } from '@features/usuarios/services/usuarios.service';

// Response del backend
interface BackendCliente {
  clienteId: number;
  usuarioId?: number;
  contacto?: string;
  direccion?: string;
  sucursal?: string;
  // Campos planos (Clean Architecture)
  nombre?: string;
  email?: string;
  telefono?: string;
  estado?: boolean;
  // Campos anidados (Legacy)
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
export class ClienteService {
  private readonly apiUrl = config.api.endpoints.clientes;

  constructor(
    private httpService: HttpService,
    private usuarioService: UsuarioService
  ) {}

  /**
   * Mapea un cliente del backend al formato del frontend
   */
  private mapBackendToFrontend(backendCliente: BackendCliente): Cliente {
    return {
      cliente_id: backendCliente.clienteId,
      clienteId: backendCliente.clienteId,
      usuarioId: backendCliente.usuarioId,
      nombre: backendCliente.nombre || backendCliente.usuario?.nombre || '',
      contacto: backendCliente.contacto,
      telefono: backendCliente.telefono || backendCliente.usuario?.telefono || '-',
      email: backendCliente.email || backendCliente.usuario?.email || '-',
      activo: backendCliente.estado ?? backendCliente.usuario?.estado ?? true,
      direccion: backendCliente.direccion,
      sucursal: backendCliente.sucursal,
      usuario: backendCliente.usuario,
    };
  }

  /**
   * Obtiene la lista de clientes con filtros opcionales
   */
  getClientes(filters?: ClienteFilters): Observable<PagedResponse<Cliente>> {
    const params: any = {
      pageNumber: filters?.pageNumber || 1,
      pageSize: filters?.pageSize || 10,
    };

    return this.httpService.get<PagedResponse<BackendCliente> | BackendCliente[]>(this.apiUrl, params).pipe(
      map(response => {
        let data: BackendCliente[] = [];
        let totalCount = 0;
        let pageNumber = params.pageNumber;
        let pageSize = params.pageSize;
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

        const mappedData = data.map(c => this.mapBackendToFrontend(c));

        // Local filtering (workaround)
        let filteredData = mappedData;
        if (filters?.search) {
          const searchTerm = filters.search.toLowerCase();
          filteredData = filteredData.filter(
            c =>
              c.nombre.toLowerCase().includes(searchTerm) ||
              (c.contacto ?? '').toLowerCase().includes(searchTerm) ||
              (c.email ?? '').toLowerCase().includes(searchTerm) ||
              (c.telefono ?? '').includes(searchTerm)
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

  /**
   * Obtiene un cliente por su ID
   */
  getClienteById(id: number): Observable<Cliente> {
    return this.httpService
      .get<BackendCliente>(`${this.apiUrl}/${id}`)
      .pipe(map(c => this.mapBackendToFrontend(c)));
  }

  /**
   * Obtiene clientes por usuarioId
   */
  getClientesByUsuario(usuarioId: number): Observable<Cliente[]> {
    return this.httpService
      .get<BackendCliente[]>(`${this.apiUrl}/usuario/${usuarioId}`)
      .pipe(map(items => items.map(c => this.mapBackendToFrontend(c))));
  }

  /**
   * Busca un cliente por nombre
   */
  buscarClientePorNombre(nombre: string): Observable<Cliente> {
    return this.httpService.get<BackendCliente[]>(this.apiUrl).pipe(
      map(clientes => {
        const found = clientes.find(c =>
          c.usuario?.nombre.toLowerCase().includes(nombre.toLowerCase())
        );
        if (!found) {
          throw new Error('Cliente no encontrado');
        }
        return this.mapBackendToFrontend(found);
      })
    );
  }

  /**
   * Crea un nuevo cliente
   */
  createCliente(cliente: CreateClienteRequest): Observable<Cliente> {
    return this.httpService
      .post<BackendCliente>(this.apiUrl, cliente)
      .pipe(map(c => this.mapBackendToFrontend(c)));
  }

  /**
   * Crea un cliente completo (Usuario + Cliente) con rollback
   */
  createClienteConUsuario(datos: {
    nombre: string;
    cedula: number;
    email: string;
    telefono?: string;
    contacto?: string;
    direccion?: string;
    sucursal?: string;
  }): Observable<Cliente> {
    // 1. Preparar datos del usuario
    const usuarioRequest = {
      rolId: 5, // Rol Cliente (ID 5 en la base de datos)
      nombre: datos.nombre,
      cedula: datos.cedula,
      email: datos.email,
      password: 'Cliente123!', // Password por defecto
      telefono: datos.telefono || undefined,
      estado: true,
    };

    console.log('📤 Request a API Usuarios:', usuarioRequest);

    // 2. Crear Usuario
    return this.usuarioService.createUsuario(usuarioRequest).pipe(
      switchMap(usuario => {
        // 3. Preparar datos del cliente
        const clienteRequest: CreateClienteRequest = {
          usuarioId: usuario.usuario_id,
          contacto: datos.contacto,
          direccion: datos.direccion,
          sucursal: datos.sucursal,
        };

        // 4. Crear Cliente
        return this.createCliente(clienteRequest).pipe(
          catchError(error => {
            console.error(
              'Error al crear cliente, revirtiendo usuario...',
              error
            );
            // 5. Rollback: Eliminar usuario si falla la creación del cliente
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
   * Actualiza un cliente existente
   */
  updateCliente(id: number, cliente: UpdateClienteRequest): Observable<void> {
    return this.httpService.put<void>(`${this.apiUrl}/${id}`, cliente);
  }

  /**
   * Elimina un cliente y su usuario asociado
   */
  deleteCliente(id: number): Observable<boolean> {
    // 1. Primero obtener el cliente para saber el usuarioId
    return this.getClienteById(id).pipe(
      switchMap(cliente => {
        const usuarioId = cliente.usuario?.usuarioId;
        // 2. Eliminar el cliente
        return this.httpService.delete<void>(`${this.apiUrl}/${id}`).pipe(
          switchMap(() => {
            // 3. Si hay usuario asociado, eliminarlo también
            if (usuarioId) {
              console.log(
                `🗑️ Eliminando usuario ${usuarioId} asociado al cliente`
              );
              return this.usuarioService.deleteUsuario(usuarioId).pipe(
                map(() => true),
                catchError(error => {
                  console.error('Error al eliminar usuario asociado:', error);
                  // Aún así retornamos true porque el cliente ya fue eliminado
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
        console.error('Error al eliminar cliente:', error);
        throw error;
      })
    );
  }

  /**
   * Da de baja un cliente (desactiva el usuario asociado)
   */
  deactivateCliente(id: number): Observable<boolean> {
    // Primero obtener el cliente para tener el usuarioId
    return this.getClienteById(id).pipe(
      switchMap(cliente => {
        if (!cliente.usuarioId) {
          throw new Error('El cliente no tiene un usuario asociado');
        }
        return this.usuarioService.deactivateUsuario(cliente.usuarioId);
      }),
      map(() => true)
    );
  }

  /**
   * Activa un cliente (activa el usuario asociado)
   */
  activateCliente(id: number): Observable<boolean> {
    // Primero obtener el cliente para tener el usuarioId
    return this.getClienteById(id).pipe(
      switchMap(cliente => {
        if (!cliente.usuarioId) {
          throw new Error('El cliente no tiene un usuario asociado');
        }
        return this.usuarioService.activateUsuario(cliente.usuarioId);
      }),
      map(() => true)
    );
  }
}
