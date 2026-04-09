import { Injectable } from '@angular/core';
import { Observable, map, forkJoin, switchMap, of, catchError } from 'rxjs';
import { HttpService } from '@core/services/http.service';
import { config } from '@environments/environment';
import { PagedResponse } from '@core/models/pagination.models';
import {
  Inventario,
  CreateInventarioRequest,
  UpdateInventarioRequest,
  InventarioFilters,
} from '../models/inventario.models';
import { ProductoService } from '@features/productos/services/productos.service';

interface BackendInventario {
  inventarioId: number;
  productoId: number;
  ubicacion: string;
  cantidad: number;
  puntoReorden: number;
  estado: boolean;
}

interface BackendInventarioStockBajo {
  inventarioId: number;
  productoId: number;
  ubicacion: string;
  cantidad: number;
  puntoReorden: number;
  estado: boolean;
  productoNombre: string;
  productoCodigo: string;
}

@Injectable({
  providedIn: 'root',
})
export class InventarioService {
  private readonly apiUrl = `${config.api.baseUrl}/api/Inventarios`;

  constructor(
    private httpService: HttpService,
    private productoService: ProductoService
  ) {}

  private mapBackendToFrontend(
    backendInventario: BackendInventario
  ): Inventario {
    return {
      inventarioId: backendInventario.inventarioId,
      productoId: backendInventario.productoId,
      ubicacion: backendInventario.ubicacion,
      cantidad: backendInventario.cantidad,
      puntoReorden: backendInventario.puntoReorden,
      estado: backendInventario.estado,
    };
  }

  private mapStockBajoToFrontend(
    backendAlerta: BackendInventarioStockBajo
  ): Inventario {
    return {
      inventarioId: backendAlerta.inventarioId,
      productoId: backendAlerta.productoId,
      ubicacion: backendAlerta.ubicacion,
      cantidad: backendAlerta.cantidad,
      puntoReorden: backendAlerta.puntoReorden,
      estado: backendAlerta.estado,
      producto: {
        productoId: backendAlerta.productoId,
        codigo: backendAlerta.productoCodigo,
        nombre: backendAlerta.productoNombre,
      },
    };
  }

  getInventariosBajoMinimo(limit = 50): Observable<Inventario[]> {
    return this.httpService
      .get<BackendInventarioStockBajo[]>(`${this.apiUrl}/alertas/stock-bajo`, {
        limit,
      })
      .pipe(map(alertas => alertas.map(alerta => this.mapStockBajoToFrontend(alerta))));
  }

  getInventarios(
    filters?: InventarioFilters
  ): Observable<PagedResponse<Inventario>> {
    const params: any = {
      pageNumber: filters?.pageNumber || 1,
      pageSize: filters?.pageSize || 10,
    };

    if (filters?.estado !== undefined) {
      params.estado = filters.estado;
    }

    return this.httpService.get<PagedResponse<BackendInventario>>(this.apiUrl, params).pipe(
      switchMap(paged => {
        if (paged.data.length === 0) {
          return of({ ...paged, data: [] });
        }

        // Obtener IDs de productos únicos para consultarlos
        const productIds = [...new Set(paged.data.map(i => i.productoId))];

        // Consultar detalles de productos en paralelo
        return forkJoin(
          productIds.map(id =>
            this.productoService.getProductoById(id).pipe(
              catchError(() => of(null)) // Manejar error si no existe producto
            )
          )
        ).pipe(
          map(products => {
            const productMap = new Map();
            products.forEach(p => {
              if (p) productMap.set(p.productoId, p);
            });

            const mappedData = paged.data.map(backendInv => {
              const product = productMap.get(backendInv.productoId);
              return {
                ...this.mapBackendToFrontend(backendInv),
                producto: product
                  ? {
                      productoId: product.productoId,
                      codigo: product.codigo,
                      nombre: product.nombre,
                      subcategoriaId: product.subcategoriaId,
                    }
                  : undefined,
              };
            });

            // Filtrado local (workaround temporal hasta que backend soporte filtros)
            let filteredData = mappedData;
            if (filters?.search) {
               const term = filters.search.toLowerCase();
               filteredData = filteredData.filter(i => 
                 i.producto?.nombre.toLowerCase().includes(term) || 
                 i.producto?.codigo.toLowerCase().includes(term) ||
                 i.ubicacion.toLowerCase().includes(term)
               );
            }

            return {
              ...paged,
              data: filteredData,
            };
          })
        );
      })
    );
  }

  getInventarioById(id: number): Observable<Inventario> {
    return this.httpService
      .get<BackendInventario>(`${this.apiUrl}/${id}`)
      .pipe(map(inv => this.mapBackendToFrontend(inv)));
  }

  getInventariosByProducto(productoId: number): Observable<Inventario[]> {
    return this.httpService
      .get<BackendInventario[]>(`${this.apiUrl}/producto/${productoId}`)
      .pipe(
        map(inventarios =>
          inventarios.map(inv => this.mapBackendToFrontend(inv))
        )
      );
  }

  createInventario(
    inventario: CreateInventarioRequest
  ): Observable<Inventario> {
    return this.httpService
      .post<BackendInventario>(this.apiUrl, inventario)
      .pipe(map(inv => this.mapBackendToFrontend(inv)));
  }

  updateInventario(
    id: number,
    inventario: UpdateInventarioRequest
  ): Observable<void> {
    return this.httpService.put<void>(`${this.apiUrl}/${id}`, inventario);
  }

  deleteInventario(id: number): Observable<void> {
    return this.httpService.delete<void>(`${this.apiUrl}/${id}`);
  }

  activateInventario(id: number): Observable<void> {
    return this.httpService.post<void>(`${this.apiUrl}/${id}/activate`, {});
  }

  deactivateInventario(id: number): Observable<void> {
    return this.httpService.post<void>(`${this.apiUrl}/${id}/deactivate`, {});
  }
}
