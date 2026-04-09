import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { map, catchError } from 'rxjs/operators';
import { HttpService } from '@core/services/http.service';
import { config } from '@environments/environment';
import { PagedResponse } from '@core/models/pagination.models';
import { BackendMovimiento, Movimiento, MovimientoFilters } from '../models/movimiento.models';

@Injectable({
  providedIn: 'root',
})
export class MovimientosService {
  private readonly apiUrl = config.api.endpoints.movimientos;

  constructor(private httpService: HttpService) {}

  /**
   * Mapea un movimiento del backend al formato del frontend
   * Nota: El backend actualmente no devuelve nombre de producto directamente,
   * por lo que necesitaremos obtenerlo del inventario o dejarlo vacío
   */
  private mapBackendToFrontend(backendMov: BackendMovimiento): Movimiento {
    return {
      id: backendMov.movimientoId,
      productoId: backendMov.inventarioId, // Usamos inventarioId como referencia
      productoNombre:
        backendMov.productoNombre && backendMov.productoCodigo
          ? `${backendMov.productoNombre} (${backendMov.productoCodigo})`
          : `Producto #${backendMov.inventarioId}`, // Fallback si no hay datos
      cantidad: backendMov.cantidad,
      tipo: backendMov.tipo.toLowerCase() === 'entrada' ? 'entrada' : 'salida',
      fecha: new Date(backendMov.fechaMovimiento),
      inventarioId: backendMov.inventarioId,
      usuarioId: backendMov.usuarioId,
      lote: backendMov.lote,
      estado: backendMov.estado,
      ordenId: backendMov.ordenId,
      proveedorId: backendMov.proveedorId,
      clienteId: backendMov.clienteId,
      // Mostrar el nombre del usuario si existe, sino el ID
      asignadoA: backendMov.usuarioNombre || `${backendMov.usuarioId}`,
      // Usar nombres reales de proveedor/cliente si existen
      proveedor:
        backendMov.proveedorNombre ||
        (backendMov.proveedorId
          ? `Proveedor #${backendMov.proveedorId}`
          : undefined),
      cliente:
        backendMov.clienteNombre ||
        (backendMov.clienteId ? `Cliente #${backendMov.clienteId}` : undefined),
    };
  }

  /**
   * Obtiene todos los movimientos con filtros
   */
  getMovimientos(filters?: MovimientoFilters): Observable<PagedResponse<Movimiento>> {
    const params: any = {
      pageNumber: filters?.pageNumber || 1,
      pageSize: filters?.pageSize || 10,
      estado: true
    };

    if (filters?.tipo) {
      params.tipo = filters.tipo;
    }
    if (filters?.search) {
      params.search = filters.search;
    }
    if (filters?.fechaInicio) {
      params.fechaInicio = filters.fechaInicio;
    }
    if (filters?.fechaFin) {
      params.fechaFin = filters.fechaFin;
    }
    if (filters?.inventarioId) {
      params.inventarioId = filters.inventarioId;
    }

    return this.httpService.get<PagedResponse<BackendMovimiento>>(this.apiUrl, params).pipe(
      map(paged => {
        // Mapear del backend al frontend
        const mapped = paged.data.map(m => this.mapBackendToFrontend(m));

        return {
          ...paged,
          data: mapped
        };
      }),
      catchError(error => {
        console.error('Error al cargar movimientos:', error);
        throw error;
      })
    );
  }

  /**
   * Obtiene movimientos por inventario
   */
  getMovimientosByInventario(inventarioId: number): Observable<Movimiento[]> {
    return this.httpService
      .get<BackendMovimiento[]>(`${this.apiUrl}/inventario/${inventarioId}`)
      .pipe(
        map(movimientos =>
          movimientos
            .map(m => this.mapBackendToFrontend(m))
            .filter(m => m.estado === true)
        ),
        catchError(error => {
          console.error(
            `Error al cargar movimientos del inventario ${inventarioId}:`,
            error
          );
          throw error;
        })
      );
  }

  /**
   * Obtiene un movimiento por ID
   */
  getMovimientoById(id: number): Observable<Movimiento> {
    return this.httpService.get<BackendMovimiento>(`${this.apiUrl}/${id}`).pipe(
      map(m => this.mapBackendToFrontend(m)),
      catchError(error => {
        console.error(`Error al cargar movimiento ${id}:`, error);
        throw error;
      })
    );
  }
}
