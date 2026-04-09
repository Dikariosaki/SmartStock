import { Injectable } from '@angular/core';
import { Observable, map, forkJoin, switchMap } from 'rxjs';
import { HttpService } from '@core/services/http.service';
import { config } from '@environments/environment';
import { ProveedorService } from '@features/proveedores/services/proveedores.service';
import { ProductoService } from '@features/productos/services/productos.service';
import { PagedResponse } from '@core/models/pagination.models';

interface BackendOrden {
  ordenId: number;
  proveedorId: number;
  fechaCreacion: string;
  estado: string;
}

export interface OrdenProvision {
  ordenId: number;
  proveedorId: number;
  proveedorNombre: string;
  fechaCreacion: string;
  estado: string;
}

export interface OrdenProvisionItem {
  ordenId: number;
  productoId: number;
  productoNombre: string;
  proveedorNombre: string;
  fechaCreacion: string;
  ordenEstado: string;
  cantidadPedida: number;
  precioCompraUnitario: number;
  estado: boolean;
}

@Injectable({ providedIn: 'root' })
export class ProvisionesService {
  private readonly apiUrl = `${config.api.baseUrl}/api/OrdenesReabastecimiento`;

  constructor(
    private httpService: HttpService,
    private proveedorService: ProveedorService,
    private productoService: ProductoService
  ) {}

  getOrdenes(page: number, limit: number): Observable<{ data: OrdenProvision[], totalCount: number }> {
    const params = {
      pageNumber: page,
      pageSize: limit
    };
    return forkJoin({
      paged: this.httpService.get<PagedResponse<BackendOrden> | BackendOrden[]>(this.apiUrl, params),
      proveedores: this.proveedorService.getProveedores(1, 1000),
    }).pipe(
      map(({ paged, proveedores }) => {
        // Handle paged response safely
        let ordenesData: BackendOrden[] = [];
        let totalCount = 0;
        
        if (Array.isArray(paged)) {
            ordenesData = paged;
            totalCount = paged.length;
        } else {
            ordenesData = paged.data || [];
            totalCount = paged.totalCount || 0;
        }

        const provs = proveedores.data;
        const data = ordenesData.map(o => {
          const p = provs.find(x => x.proveedorId === o.proveedorId);
          return {
            ordenId: o.ordenId,
            proveedorId: o.proveedorId,
            proveedorNombre: p ? p.nombre : `Proveedor ${o.proveedorId}`,
            fechaCreacion: o.fechaCreacion,
            estado: o.estado,
          };
        });
        return { data, totalCount: totalCount };
      })
    );
  }

  completarOrden(orden: OrdenProvision): Observable<void> {
    const body = { proveedorId: orden.proveedorId, estado: 'Completada' };
    return this.httpService.put<void>(`${this.apiUrl}/${orden.ordenId}`, body);
  }

  getItems(page: number, limit: number): Observable<{ data: OrdenProvisionItem[], totalCount: number }> {
    return this.getOrdenes(page, limit).pipe(
      switchMap(result => {
        const ordenes = result.data;
        if (ordenes.length === 0) {
          return new Observable<{ data: OrdenProvisionItem[], totalCount: number }>(observer => {
            observer.next({ data: [], totalCount: result.totalCount });
            observer.complete();
          });
        }
        return forkJoin({
          productos: this.productoService.getProductos({ pageNumber: 1, pageSize: 1000 }),
          itemsPorOrden: forkJoin(
            ordenes.map(o =>
              this.httpService
                .get<any[]>(`${config.api.baseUrl}/api/OrdenReabastecimientoProductos/orden/${o.ordenId}`)
                .pipe(
                  map(items =>
                    items.map(it => ({
                      ...it,
                      ordenId: o.ordenId,
                      proveedorNombre: o.proveedorNombre,
                      fechaCreacion: o.fechaCreacion,
                      ordenEstado: o.estado,
                    }))
                  )
                )
            )
          ),
        }).pipe(
          map(({ productos, itemsPorOrden }) => {
            let productList: any[] = [];
            if (Array.isArray(productos)) {
              productList = productos;
            } else if ((productos as any).data) {
              productList = (productos as any).data;
            }

            const flat = ([] as any[]).concat(...itemsPorOrden);
            const data = flat.map(row => {
              const p = productList.find(pr => pr.productoId === row.productoId);
              return {
                ordenId: row.ordenId,
                productoId: row.productoId,
                productoNombre: p ? p.nombre : `Producto ${row.productoId}`,
                proveedorNombre: row.proveedorNombre,
                fechaCreacion: row.fechaCreacion,
                ordenEstado: row.ordenEstado,
                cantidadPedida: row.cantidad,
                precioCompraUnitario: row.precioUnitario,
                estado: row.estado || false,
              } as OrdenProvisionItem;
            });
            return { data, totalCount: result.totalCount };
          })
        );
      })
    );
  }

  completarItem(ordenId: number, productoId: number): Observable<void> {
    const url = `${config.api.baseUrl}/api/OrdenReabastecimientoProductos/${ordenId}/${productoId}/activate`;
    return this.httpService.post<void>(url, {});
  }
}
