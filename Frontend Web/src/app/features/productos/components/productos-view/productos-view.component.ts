import { Component, OnInit, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Subject, debounceTime, distinctUntilChanged, takeUntil } from 'rxjs';
import { ProductoService } from '../../services/productos.service';
import { Producto, ProductoFilters } from '../../models/productos.models';
import { ProductosModalComponent } from '../productos-modal/productos-modal.component';
import { SidebarComponent } from '../../../../shared/ui/layout/sidebar/sidebar.component';
import { StockModalComponent } from '../stock-modal/stock-modal.component';
import { AveriaModalComponent } from '../averia-modal/averia-modal.component';
import { BellNotificationComponent } from '@shared/ui/notifications/bell-notification.component';
import { PermissionService } from '@core/auth/permission.service';
import { NotificationService } from '@shared/services/notification.service';

@Component({
  selector: 'app-productos-view',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    ProductosModalComponent,
    SidebarComponent,
    StockModalComponent,
    AveriaModalComponent,
    BellNotificationComponent,
  ],
  templateUrl: './productos-view.component.html',
  styleUrls: ['./productos-view.component.css'],
})

export class ProductosViewComponent implements OnInit, OnDestroy {
  private destroy$ = new Subject<void>();
  private search$ = new Subject<string>();

  productos: Producto[] = [];
  filtros: ProductoFilters = {};
  paginacion = { page: 1, limit: 10, total: 0 };
  loading = false;
  searchTerm = '';
  selectedEstado: string = 'all';
  showFilterMenu = false;

  selectedProducto: Producto | null = null;
  showModal = false;
  modalMode: 'create' | 'edit' | 'view' = 'create';

  showDeleteConfirm = false;
  showDeleteSuccess = false;
  productoAEliminar: Producto | null = null;

  showDeactivateConfirm = false;
  showDeactivateSuccess = false;
  productoADarDeBaja: Producto | null = null;

  Math = Math;

  constructor(
    private productoService: ProductoService,
    private permissionService: PermissionService,
    private notificationService: NotificationService
  ) {}

  get canCreateProducto(): boolean {
    return this.permissionService.canAction('productos', 'create');
  }

  get canEditProducto(): boolean {
    return this.permissionService.canAction('productos', 'edit');
  }

  get canDeleteProducto(): boolean {
    return this.permissionService.canAction('productos', 'delete');
  }

  get canDeactivateProducto(): boolean {
    return this.permissionService.canAction('productos', 'deactivate');
  }

  get canActivateProducto(): boolean {
    return this.permissionService.canAction('productos', 'activate');
  }

  get canStockEntrada(): boolean {
    return this.permissionService.canAction('productos', 'stockEntrada');
  }

  get canStockSalida(): boolean {
    return this.permissionService.canAction('productos', 'stockSalida');
  }

  get canReportAveria(): boolean {
    return this.permissionService.canAction('productos', 'reportAveria');
  }

  toggleDarkMode(): void {
    document.documentElement.classList.toggle('dark');
  }

  ngOnInit(): void {
    this.cargar();
    this.search$
      .pipe(debounceTime(300), distinctUntilChanged(), takeUntil(this.destroy$))
      .subscribe(t => {
        this.filtros.search = t;
        this.cargar();
      });
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }

  cargar(): void {
    this.loading = true;

    // Mapeo del estado seleccionado a los filtros
    if (this.selectedEstado === 'active') {
      this.filtros.estado = true;
    } else if (this.selectedEstado === 'inactive') {
      this.filtros.estado = false;
    } else {
      this.filtros.estado = undefined;
    }

    const filters: ProductoFilters = {
      ...this.filtros,
      pageNumber: this.paginacion.page,
      pageSize: this.paginacion.limit,
    };

    this.productoService.getProductos(filters).subscribe({
      next: (res: any) => {
        // La respuesta ahora es PagedResponse
        this.productos = res.data;
        this.paginacion.total = res.totalCount;
        this.loading = false;
      },
      error: () => (this.loading = false),
    });
  }

  toggleFilterMenu(): void {
    this.showFilterMenu = !this.showFilterMenu;
  }

  selectEstado(estado: string): void {
    this.selectedEstado = estado;
    this.showFilterMenu = false;
    this.paginacion.page = 1;
    this.cargar();
  }

  crearProducto(): void {
    if (
      !this.permissionService.guardAction(
        'productos',
        'create',
        'No tienes permisos para crear productos.'
      )
    ) {
      return;
    }

    this.selectedProducto = null;
    this.modalMode = 'create';
    this.showModal = true;
  }
  editar(p: Producto): void {
    if (
      !this.permissionService.guardAction(
        'productos',
        'edit',
        'No tienes permisos para editar productos.'
      )
    ) {
      return;
    }

    this.selectedProducto = p;
    this.modalMode = 'edit';
    this.showModal = true;
  }

  onModalSuccess(): void {
    // <-- Borré la p
    this.showModal = false;
    this.cargar();
  }
  closeModal(): void {
    this.showModal = false;
  }

  // --- ELIMINAR ---
  eliminar(p: Producto): void {
    if (
      !this.permissionService.guardAction(
        'productos',
        'delete',
        'No tienes permisos para eliminar productos.'
      )
    ) {
      return;
    }

    console.log('📢 ¡CLIC DETECTADO! Intentando eliminar a:', p.nombre); // <--- MENSAJE 1

    this.productoAEliminar = p;
    this.showDeleteConfirm = true;

    console.log(
      '✅ Variable showDeleteConfirm ahora es:',
      this.showDeleteConfirm
    ); // <--- MENSAJE 2
  }

  confirmarEliminacion(): void {
    if (
      !this.permissionService.guardAction(
        'productos',
        'delete',
        'No tienes permisos para eliminar productos.'
      )
    ) {
      return;
    }

    if (!this.productoAEliminar) return;
    this.loading = true;
    this.productoService
      .deleteProducto(this.productoAEliminar.productoId)
      .subscribe({
        next: () => {
          this.loading = false;
          this.showDeleteConfirm = false;
          this.showDeleteSuccess = true;
          this.cargar();
        },
        error: () => {
          this.loading = false;
          this.showDeleteConfirm = false;
        },
      });
  }

  cancelarEliminacion(): void {
    this.showDeleteConfirm = false;
    this.productoAEliminar = null;
  }
  cerrarModalEliminacion(): void {
    this.showDeleteSuccess = false;
  }

  // --- DAR DE BAJA / ACTIVAR ---
  darDeBaja(p: Producto): void {
    if (
      !this.permissionService.guardAction(
        'productos',
        'deactivate',
        'No tienes permisos para desactivar productos.'
      )
    ) {
      return;
    }

    this.productoADarDeBaja = p;
    this.showDeactivateConfirm = true;
  }

  confirmarDarDeBaja(): void {
    if (
      !this.permissionService.guardAction(
        'productos',
        'deactivate',
        'No tienes permisos para desactivar productos.'
      )
    ) {
      return;
    }

    if (!this.productoADarDeBaja) return;

    this.loading = true;

    this.productoService
      .deactivateProducto(this.productoADarDeBaja.productoId)
      .subscribe({
        next: () => {
          this.loading = false;
          this.showDeactivateConfirm = false;
          this.showDeactivateSuccess = true;

          // Si estamos mostrando activos, remover el desactivado
          if (this.selectedEstado === 'active') {
             this.productos = this.productos.filter(
              p => p.productoId !== this.productoADarDeBaja?.productoId
            );
          } else {
            // Si mostramos todos, actualizar estado visualmente
            const prod = this.productos.find(p => p.productoId === this.productoADarDeBaja?.productoId);
            if (prod) prod.estado = false;
          }

          this.cargar();
        },
        error: () => {
          this.loading = false;
          this.showDeactivateConfirm = false;
        },
      });
  }

  activarProducto(p: Producto): void {
    if (
      !this.permissionService.guardAction(
        'productos',
        'activate',
        'No tienes permisos para activar productos.'
      )
    ) {
      return;
    }

    this.loading = true;
    this.productoService.activateProducto(p.productoId).subscribe({
      next: () => {
        this.loading = false;
        // Si estamos mostrando inactivos, remover el activado
        if (this.selectedEstado === 'inactive') {
          this.productos = this.productos.filter(
            prod => prod.productoId !== p.productoId
          );
        } else {
           // Si mostramos todos, actualizar estado visualmente
           const prod = this.productos.find(prod => prod.productoId === p.productoId);
           if (prod) prod.estado = true;
        }
        this.cargar();
      },
      error: () => {
        this.loading = false;
      }
    });
  }

  cancelarDarDeBaja(): void {
    this.showDeactivateConfirm = false;
    this.productoADarDeBaja = null;
  }
  cerrarModalDarDeBaja(): void {
    this.showDeactivateSuccess = false;
  }

  // --- UTILIDADES ---
  onSearch(): void {
    this.search$.next(this.searchTerm);
  }
  onSearchKeyPress(e: KeyboardEvent): void {
    if (e.key === 'Enter') this.onSearch();
  }
  onSearchEvent(e: Event): void {
    this.searchTerm = (e.target as HTMLInputElement).value;
    this.onSearch();
  }

  trackById(i: number, p: Producto): number {
    return p.productoId;
  }

  // Reorden encapsulado

  get totalPages(): number {
    return Math.ceil(this.paginacion.total / this.paginacion.limit) || 1;
  }
  previousPage(): void {
    if (this.paginacion.page > 1) {
      this.paginacion.page--;
      this.cargar();
    }
  }
  nextPage(): void {
    if (this.paginacion.page < this.totalPages) {
      this.paginacion.page++;
      this.cargar();
    }
  }
  getPageNumbers(): number[] {
    return Array.from({ length: this.totalPages }, (_, i) => i + 1);
  }
  changePage(page: number): void {
    this.paginacion.page = page;
    this.cargar();
  }
  getFillerRows(): number[] {
    return Array(Math.max(0, this.paginacion.limit - this.productos.length))
      .fill(0)
      .map((_, i) => i);
  }
  getNoDataMessage(): string {
    return 'No se encontraron productos';
  }

  onBackdropClick(event: Event): void {
    if (event.target === event.currentTarget) {
      if (this.showDeleteConfirm) this.cancelarEliminacion();
      else if (this.showDeleteSuccess) this.cerrarModalEliminacion();
      else if (this.showDeactivateConfirm) this.cancelarDarDeBaja();
      else if (this.showDeactivateSuccess) this.cerrarModalDarDeBaja();
    }
  }
  // VARIABLES PARA STOCK
  showStockModal = false;
  stockMode: 'entrada' | 'salida' = 'entrada';
  selectedRowProducto: Producto | null = null; // Para saber cuál fila clicaste

  // ...

  // FUNCIÓN PARA SELECCIONAR FILA (Al hacer clic en la tabla)
  seleccionarFila(p: Producto): void {
    if (this.selectedRowProducto === p) {
      // Si ya estaba seleccionado, lo quitamos (Deseleccionar)
      this.selectedRowProducto = null;
    } else {
      // Si era otro o ninguno, lo seleccionamos
      this.selectedRowProducto = p;
    }
  }

  // FUNCIÓN PARA ABRIR EL MODAL DE LOS BOTONES GRANDES
  abrirModalStock(modo: 'entrada' | 'salida'): void {
    const action = modo === 'entrada' ? 'stockEntrada' : 'stockSalida';
    const deniedMessage =
      modo === 'entrada'
        ? 'No tienes permisos para registrar entradas de stock.'
        : 'No tienes permisos para registrar salidas de stock.';

    if (!this.permissionService.guardAction('productos', action, deniedMessage)) {
      return;
    }

    if (!this.selectedRowProducto) {
      alert('Por favor, seleccione un producto de la tabla primero.');
      return;
    }
    this.stockMode = modo;
    this.showStockModal = true;
  }

  // FUNCIÓN QUE PROCESA EL GUARDADO
  procesarStock(event: {
    cantidad: number;
    proveedorId?: number;
    clienteId?: number;
    lote?: string;
  }): void {
    const action = this.stockMode === 'entrada' ? 'stockEntrada' : 'stockSalida';
    const deniedMessage =
      this.stockMode === 'entrada'
        ? 'No tienes permisos para registrar entradas de stock.'
        : 'No tienes permisos para registrar salidas de stock.';

    if (!this.permissionService.guardAction('productos', action, deniedMessage)) {
      this.showStockModal = false;
      return;
    }

    if (!this.selectedRowProducto) return;

    this.loading = true;
    const id = this.selectedRowProducto.productoId;

    const request$ =
      this.stockMode === 'entrada'
        ? this.productoService.addStock(
            id,
            event.cantidad,
            event.proveedorId,
            event.lote
          )
        : this.productoService.removeStock(id, event.cantidad, {
            clienteId: event.clienteId,
            lote: event.lote,
          });

    request$.subscribe({
      next: () => {
        this.loading = false;
        this.showStockModal = false;
        this.notificationService.refreshInventoryAlerts();
        this.cargar(); // Recarga la tabla para ver la nueva cantidad
      },
      error: (err: any) => {
        this.loading = false;
        alert('Error: ' + err.message); // Muestra error si saca más de lo que hay
      },
    });
  }
  // VARIABLES PARA AVERÍAS
  showAveriaModal = false;
  productoParaAveria: Producto | null = null;

  // ...

  // 1. ABRIR MODAL DE AVERÍA
  abrirAveria(p: Producto): void {
    if (
      !this.permissionService.guardAction(
        'productos',
        'reportAveria',
        'No tienes permisos para reportar averias.'
      )
    ) {
      return;
    }

    this.productoParaAveria = p;
    this.showAveriaModal = true;
  }

  // 2. PROCESAR AVERÍA
  procesarAveria(datos: {
    tipo: string;
    cantidad: number;
    descripcion: string;
  }): void {
    if (
      !this.permissionService.guardAction(
        'productos',
        'reportAveria',
        'No tienes permisos para reportar averias.'
      )
    ) {
      this.showAveriaModal = false;
      return;
    }

    if (!this.productoParaAveria) return;

    this.loading = true;

    this.productoService
      .reportarAveria(
        this.productoParaAveria.productoId,
        datos.tipo,
        datos.cantidad,
        datos.descripcion
      )
      .subscribe({
        next: () => {
          this.loading = false;
          this.showAveriaModal = false;
          this.notificationService.refreshInventoryAlerts();
          this.cargar(); // Recarga para ver el stock actualizado
          alert('Reporte registrado correctamente'); // Opcional: Mensaje simple
        },
        error: (err: any) => {
          this.loading = false;
          alert('Error: ' + err.message);
        },
      });
  }
}
