import { Injectable } from '@angular/core';
import { Observable, of } from 'rxjs';
import { delay } from 'rxjs/operators';
import { HttpClient } from '@angular/common/http';

import {
  Producto,
  CreateProductoRequest,
  UpdateProductoRequest,
  ProductoFilters,
  ProductosListResponse,
  Movimiento,
} from '../models/productos.models';

@Injectable({
  providedIn: 'root',
})
export class ProductoService {
  // DATOS SIMULADOS (MOCK) - Basados en tu imagen
  private mockProductos: Producto[] = [
    {
      producto_id: 1,
      nombre: 'Merey',
      categoria: 'Unguentos',
      subcategoria: 'Topicos',
      cantidad: 1000,
      cliente: 'Cruz Verde',
      proveedor: 'Bayer',
      precio: 700,
      activo: true,
    },
    {
      producto_id: 2,
      nombre: 'Merey',
      categoria: 'Unguentos',
      subcategoria: 'Topicos',
      cantidad: 150,
      cliente: 'Cruz Verde',
      proveedor: 'Bayer',
      precio: 700,
      activo: true,
    },
    {
      producto_id: 3,
      nombre: 'Forz',
      categoria: 'Unguentos',
      subcategoria: 'Topicos',
      cantidad: 10000,
      cliente: 'Cruz Verde',
      proveedor: 'Bayer',
      precio: 700,
      activo: true,
    },
    {
      producto_id: 4,
      nombre: 'Advil',
      categoria: 'Pastas',
      subcategoria: 'Pasta',
      cantidad: 100,
      cliente: 'Cruz Verde',
      proveedor: 'Bayer',
      precio: 700,
      activo: true,
    },
    {
      producto_id: 1,
      nombre: 'Merey',
      categoria: 'Unguentos',
      subcategoria: 'Topicos',
      cantidad: 1000,
      cliente: 'Cruz Verde',
      proveedor: 'Bayer',
      precio: 700,
      activo: true,
    },
    {
      producto_id: 2,
      nombre: 'Merey',
      categoria: 'Unguentos',
      subcategoria: 'Topicos',
      cantidad: 150,
      cliente: 'Cruz Verde',
      proveedor: 'Bayer',
      precio: 700,
      activo: true,
    },
    {
      producto_id: 3,
      nombre: 'Forz',
      categoria: 'Unguentos',
      subcategoria: 'Topicos',
      cantidad: 10000,
      cliente: 'Cruz Verde',
      proveedor: 'Bayer',
      precio: 700,
      activo: true,
    },
    {
      producto_id: 4,
      nombre: 'Advil',
      categoria: 'Pastas',
      subcategoria: 'Pasta',
      cantidad: 100,
      cliente: 'Cruz Verde',
      proveedor: 'Bayer',
      precio: 700,
      activo: true,
    },
    {
      producto_id: 1,
      nombre: 'Merey',
      categoria: 'Unguentos',
      subcategoria: 'Topicos',
      cantidad: 1000,
      cliente: 'Cruz Verde',
      proveedor: 'Bayer',
      precio: 700,
      activo: true,
    },
    {
      producto_id: 2,
      nombre: 'Merey',
      categoria: 'Unguentos',
      subcategoria: 'Topicos',
      cantidad: 150,
      cliente: 'Cruz Verde',
      proveedor: 'Bayer',
      precio: 700,
      activo: true,
    },
    {
      producto_id: 3,
      nombre: 'Forz',
      categoria: 'Unguentos',
      subcategoria: 'Topicos',
      cantidad: 10000,
      cliente: 'Cruz Verde',
      proveedor: 'Bayer',
      precio: 700,
      activo: true,
    },
    {
      producto_id: 4,
      nombre: 'Advil',
      categoria: 'Pastas',
      subcategoria: 'Pasta',
      cantidad: 100,
      cliente: 'Cruz Verde',
      proveedor: 'Bayer',
      precio: 700,
      activo: true,
    },
    {
      producto_id: 1,
      nombre: 'Merey',
      categoria: 'Unguentos',
      subcategoria: 'Topicos',
      cantidad: 1000,
      cliente: 'Cruz Verde',
      proveedor: 'Bayer',
      precio: 700,
      activo: true,
    },
    {
      producto_id: 2,
      nombre: 'Merey',
      categoria: 'Unguentos',
      subcategoria: 'Topicos',
      cantidad: 150,
      cliente: 'Cruz Verde',
      proveedor: 'Bayer',
      precio: 700,
      activo: true,
    },
    {
      producto_id: 3,
      nombre: 'Forz',
      categoria: 'Unguentos',
      subcategoria: 'Topicos',
      cantidad: 10000,
      cliente: 'Cruz Verde',
      proveedor: 'Bayer',
      precio: 700,
      activo: true,
    },
    {
      producto_id: 4,
      nombre: 'Advil',
      categoria: 'Pastas',
      subcategoria: 'Pasta',
      cantidad: 100,
      cliente: 'Cruz Verde',
      proveedor: 'Bayer',
      precio: 700,
      activo: true,
    },
  ];

  constructor(private http: HttpClient) {}

  getProductos(
    page: number,
    limit: number,
    filters?: ProductoFilters
  ): Observable<ProductosListResponse> {
    const filteredData = this.mockProductos.filter(p => {
      let matches = true;

      // FILTRO DE ACTIVOS (Vital para que desaparezcan al borrar)
      matches = matches && p.activo === true;

      if (filters?.search) {
        const term = filters.search.toLowerCase();
        matches =
          matches &&
          (p.nombre.toLowerCase().includes(term) ||
            p.categoria.toLowerCase().includes(term) ||
            p.proveedor.toLowerCase().includes(term));
      }
      return matches;
    });

    const total = filteredData.length;
    const start = (page - 1) * limit;
    const data = filteredData.slice(start, start + limit);

    return of({ data, meta: { total, page, limit } }).pipe(delay(300));
  }

  createProducto(req: CreateProductoRequest): Observable<Producto> {
    // Generamos un ID alto como en tu ejemplo (125)
    const newId =
      this.mockProductos.length > 0
        ? Math.max(...this.mockProductos.map(p => p.producto_id)) + 1
        : 126;
    const newObj: Producto = { ...req, producto_id: newId, activo: true };
    this.mockProductos.push(newObj);
    return of(newObj).pipe(delay(300));
  }

  updateProducto(id: number, req: UpdateProductoRequest): Observable<Producto> {
    const index = this.mockProductos.findIndex(p => p.producto_id === id);
    if (index > -1) {
      this.mockProductos[index] = { ...this.mockProductos[index], ...req };
      return of(this.mockProductos[index]).pipe(delay(300));
    }
    throw new Error('Producto no encontrado');
  }

  deleteProducto(id: number): Observable<void> {
    const index = this.mockProductos.findIndex(p => p.producto_id === id);
    if (index > -1) {
      this.mockProductos.splice(index, 1);
      return of(void 0).pipe(delay(300));
    }
    throw new Error('Producto no encontrado');
  }

  deactivateProducto(id: number): Observable<Producto> {
    const item = this.mockProductos.find(p => p.producto_id === id);
    if (item) {
      item.activo = false;
      return of(item).pipe(delay(300));
    }
    throw new Error('Producto no encontrado');
  }
  // DISMINUIR STOCK
  removeStock(
    id: number,
    cantidad: number,
    extraData?: { direccion?: string; sucursal?: string }
  ): Observable<Producto> {
    const item = this.mockProductos.find(p => p.producto_id === id);
    if (item) {
      const actual = Number(item.cantidad) || 0;
      if (actual < cantidad) {
        throw new Error('No hay suficiente stock');
      }
      item.cantidad = actual - Number(cantidad);

      // Registrar movimiento
      this.registrarMovimiento(item, cantidad, 'salida', {
        cliente: item.cliente,
        direccion: extraData?.direccion,
        sucursal: extraData?.sucursal,
      });

      return of(item).pipe(delay(300));
    }
    throw new Error('Producto no encontrado');
  }

  // AUMENTAR STOCK
  addStock(id: number, cantidad: number): Observable<Producto> {
    const item = this.mockProductos.find(p => p.producto_id === id);
    if (item) {
      item.cantidad = (Number(item.cantidad) || 0) + Number(cantidad);

      // Registrar movimiento
      this.registrarMovimiento(item, cantidad, 'entrada', {
        proveedor: item.proveedor,
      });

      return of(item).pipe(delay(300));
    }
    throw new Error('Producto no encontrado');
  }

  // --- MOVIMIENTOS ---
  private mockMovimientos: Movimiento[] = [];

  private registrarMovimiento(
    producto: Producto,
    cantidad: number,
    tipo: 'entrada' | 'salida',
    extraData?: {
      proveedor?: string;
      cliente?: string;
      direccion?: string;
      sucursal?: string;
    }
  ) {
    const nuevoMovimiento: Movimiento = {
      id: this.mockMovimientos.length + 1,
      productoId: producto.producto_id,
      productoNombre: producto.nombre,
      cantidad: cantidad,
      tipo: tipo,
      fecha: new Date(),
      asignadoA: producto.cliente,
      ...extraData,
    };
    this.mockMovimientos.unshift(nuevoMovimiento); // Agregar al principio
  }

  getMovimientos(tipo?: 'entrada' | 'salida'): Observable<Movimiento[]> {
    let movimientos = this.mockMovimientos;
    if (tipo) {
      movimientos = movimientos.filter(m => m.tipo === tipo);
    }
    return of(movimientos).pipe(delay(300));
  }
}
