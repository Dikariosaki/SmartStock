import { Injectable } from '@angular/core';
import { Observable, map, forkJoin, switchMap, of } from 'rxjs';
import { HttpService } from '@core/services/http.service';
import { config } from '@environments/environment';
import { SubcategoriaService } from '@features/subcategorias/services/subcategorias.service';
import { CategoriaService } from '@features/categorias/services/categorias.service';
import { AuthService } from '@core/services/auth.service';

import { PagedResponse } from '@core/models/pagination.models';

import {
  Producto,
  CreateProductoRequest,
  UpdateProductoRequest,
  ProductoFilters,
} from '../models/productos.models';

interface BackendProducto {
  productoId: number;
  subcategoriaId: number;
  codigo: string;
  nombre: string;
  descripcion?: string;
  precioUnitario: number;
  estado: boolean;
}

@Injectable({
  providedIn: 'root',
})
export class ProductoService {
  private apiUrl = `${config.api.baseUrl}/api/Productos`;

  constructor(
    private httpService: HttpService,
    private subcategoriaService: SubcategoriaService,
    private categoriaService: CategoriaService,
    private authService: AuthService
  ) {}

  /**
   * Mapea un producto del backend al formato del frontend
   */
  private mapBackendToFrontend(backendProducto: BackendProducto): Producto {
    return {
      productoId: backendProducto.productoId,
      subcategoriaId: backendProducto.subcategoriaId,
      codigo: backendProducto.codigo,
      nombre: backendProducto.nombre,
      descripcion: backendProducto.descripcion || '',
      precioUnitario: backendProducto.precioUnitario,
      estado: backendProducto.estado,
    };
  }

  /**
   * Obtiene la lista de productos con información de subcategorías
   */
  getProductos(filters?: ProductoFilters): Observable<PagedResponse<Producto>> {
    const params: any = {
      pageNumber: filters?.pageNumber || 1,
      pageSize: filters?.pageSize || 10,
    };
    
    if (filters?.estado !== undefined) {
      params.estado = filters.estado;
    }

    // Cargamos productos paginados y subcategorías en paralelo
    return forkJoin({
      pagedResponse: this.httpService.get<PagedResponse<BackendProducto> | BackendProducto[]>(this.apiUrl, params),
      subcategorias: this.subcategoriaService.getSubcategorias(1, 1000), // Get all subcategories for mapping
      categorias: this.categoriaService.getCategorias(1, 1000) // Get all categories for mapping
    }).pipe(
      map(({ pagedResponse, subcategorias, categorias }) => {
        // Extract data from subcategories PagedResponse safely
        let subcategoriasList: any[] = [];
        if (Array.isArray(subcategorias)) {
            subcategoriasList = subcategorias;
        } else if ((subcategorias as any).data) {
            subcategoriasList = (subcategorias as any).data;
        }

        // Extract data from categories PagedResponse safely
        let categoriasList: any[] = [];
        if (Array.isArray(categorias)) {
            categoriasList = categorias;
        } else if ((categorias as any).data) {
            categoriasList = (categorias as any).data;
        }

        // Handle products response safely
        let data: BackendProducto[] = [];
        let totalCount = 0;
        let pageNumber = params.pageNumber;
        let pageSize = params.pageSize;
        let totalPages = 0;

        if (Array.isArray(pagedResponse)) {
            data = pagedResponse;
            totalCount = pagedResponse.length;
            totalPages = Math.ceil(totalCount / pageSize);
        } else {
            const pr = pagedResponse as PagedResponse<BackendProducto>;
            data = pr.data || [];
            totalCount = pr.totalCount || 0;
            pageNumber = pr.pageNumber || pageNumber;
            pageSize = pr.pageSize || pageSize;
            totalPages = pr.totalPages || Math.ceil(totalCount / pageSize);
        }

        // Mapeamos cada producto y le agregamos su subcategoría
        let mappedProductos = data.map((p: BackendProducto) => {
          const subcategoria = subcategoriasList.find(
            s => s.subcategoriaId === p.subcategoriaId
          );
          
          let categoriaNombre = undefined;
          if (subcategoria && subcategoria.categoriaId) {
             const categoria = categoriasList.find(c => c.categoriaId === subcategoria.categoriaId);
             if (categoria) {
                 categoriaNombre = categoria.nombre;
             }
          }

          return {
            ...this.mapBackendToFrontend(p),
            subcategoria: subcategoria
              ? {
                  subcategoriaId: subcategoria.subcategoriaId,
                  nombre: subcategoria.nombre,
                  categoriaId: subcategoria.categoriaId,
                  categoriaNombre: categoriaNombre,
                  estado: subcategoria.estado,
                }
              : undefined,
          };
        });

        // Filtrado por búsqueda en frontend (solo en la página actual)
        // Nota: Idealmente el backend debería soportar búsqueda
        if (filters?.search) {
          const term = filters.search.toLowerCase().trim();
          mappedProductos = mappedProductos.filter(
            p =>
              p.nombre.toLowerCase().includes(term) ||
              p.codigo.toLowerCase().includes(term)
          );
        }

        return {
          data: mappedProductos,
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
   * Obtiene un producto por ID con información de subcategoría
   */
  getProductoById(id: number): Observable<Producto> {
    return this.httpService.get<BackendProducto>(`${this.apiUrl}/${id}`).pipe(
      switchMap((p: BackendProducto) => {
        // Cargamos la subcategoría del producto
        return this.subcategoriaService
          .getSubcategoriaById(p.subcategoriaId)
          .pipe(
            switchMap(subcategoria => {
                // Si tiene subcategoría, buscamos la categoría padre
                if (subcategoria.categoriaId) {
                    return this.categoriaService.getCategoriaById(subcategoria.categoriaId).pipe(
                        map(categoria => ({
                            ...this.mapBackendToFrontend(p),
                            subcategoria: {
                                subcategoriaId: subcategoria.subcategoriaId,
                                nombre: subcategoria.nombre,
                                categoriaId: subcategoria.categoriaId,
                                categoriaNombre: categoria.nombre,
                                estado: subcategoria.estado,
                            }
                        }))
                    );
                }
                
                // Si no hay categoriaId (raro), devolvemos sin nombre de categoría
                return of({
                    ...this.mapBackendToFrontend(p),
                    subcategoria: {
                        subcategoriaId: subcategoria.subcategoriaId,
                        nombre: subcategoria.nombre,
                        categoriaId: subcategoria.categoriaId,
                        estado: subcategoria.estado,
                    }
                });
            })
          );
      })
    );
  }

  /**
   * Crea un nuevo producto
   */
  createProducto(req: CreateProductoRequest): Observable<Producto> {
    return this.httpService
      .post<BackendProducto>(this.apiUrl, req)
      .pipe(map((p: BackendProducto) => this.mapBackendToFrontend(p)));
  }

  /**
   * Actualiza un producto existente
   */
  updateProducto(id: number, req: UpdateProductoRequest): Observable<void> {
    return this.httpService.put<void>(`${this.apiUrl}/${id}`, req);
  }

  /**
   * Elimina un producto
   */
  deleteProducto(id: number): Observable<void> {
    return this.httpService.delete<void>(`${this.apiUrl}/${id}`);
  }

  /**
   * Activa un producto
   */
  activateProducto(id: number): Observable<void> {
    return this.httpService.post<void>(`${this.apiUrl}/${id}/activate`, {});
  }

  /**
   * Desactiva un producto
   */
  deactivateProducto(id: number): Observable<void> {
    return this.httpService.post<void>(`${this.apiUrl}/${id}/deactivate`, {});
  }

  // --- STOCK Y MOVIMIENTOS ---

  addStock(
    id: number,
    cantidad: number,
    proveedorId?: number,
    lote?: string
  ): Observable<any> {
    const currentUser = this.authService.currentUserValue;
    const usuarioId = currentUser ? currentUser.id : 1; // Fallback to 1 if no user (should not happen in protected routes)

    return this.httpService.post(
      `${config.api.baseUrl}/api/Inventarios/entrada`,
      { productoId: id, cantidad, proveedorId, lote, usuarioId }
    );
  }

  removeStock(id: number, cantidad: number, extraData?: any): Observable<any> {
    const currentUser = this.authService.currentUserValue;
    const usuarioId = currentUser ? currentUser.id : 1;

    return this.httpService.post(
      `${config.api.baseUrl}/api/Inventarios/salida`,
      { productoId: id, cantidad, usuarioId, ...extraData }
    );
  }

  reportarAveria(
    id: number,
    tipo: string,
    cantidad: number,
    descripcion: string
  ): Observable<any> {
    return this.httpService.post(`${config.api.baseUrl}/api/Reportes`, {
      productoId: id,
      tipoReporte: 'Avería',
      origen: tipo,
      cantidad,
      descripcion,
    });
  }

  getMovimientos(_tipo?: string): Observable<any[]> {
    _tipo = 'entrada';
    return of([]);
    // Cuando tengas el endpoint real, sería:
    // return this.httpService.get<any[]>(`${config.api.baseUrl}/api/Movimientos?tipo=${tipo}`);
  }
}
