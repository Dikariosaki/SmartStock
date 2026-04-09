import { Component, OnInit, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Subject, takeUntil, debounceTime, distinctUntilChanged } from 'rxjs';
import { InventarioService } from '../../services/inventarios.service';
import { Inventario, InventarioFilters } from '../../models/inventario.models';
import { InventarioModalComponent } from '../inventario-modal/inventario-modal.component';
import { SidebarComponent } from '@shared/ui/layout/sidebar/sidebar.component';
import { BellNotificationComponent } from '@shared/ui/notifications/bell-notification.component';
import { PermissionService } from '@core/auth/permission.service';
import { NotificationService } from '@shared/services/notification.service';

@Component({
  selector: 'app-inventario-list',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    InventarioModalComponent,
    SidebarComponent,
    BellNotificationComponent,
  ],
  templateUrl: './inventario-list.component.html',
  styleUrl: './inventario-list.component.css',
})
export class InventarioListComponent implements OnInit, OnDestroy {
  private destroy$ = new Subject<void>();
  private searchSubject = new Subject<string>();
  inventarios: Inventario[] = [];
  filtros: InventarioFilters = {};
  paginacion = { page: 1, limit: 10, total: 0 };
  loading = false;
  error: string | null = null;
  searchTerm = '';
  selectedEstado: string = 'all';
  showFilterMenu = false;
  showModal = false;
  modalMode: 'create' | 'edit' | 'view' = 'create';
  inventarioSeleccionado: Inventario | null = null;
  // Reorden encapsulado dentro de BellNotification
  showDeleteConfirm = false;
  showDeactivateConfirm = false;
  inventarioAEliminar: Inventario | null = null;
  inventarioADesactivar: Inventario | null = null;
  Math = Math;

  toggleDarkMode(): void {
    document.documentElement.classList.toggle('dark');
  }

  constructor(
    private inventarioService: InventarioService,
    private permissionService: PermissionService,
    private notificationService: NotificationService
  ) {}

  get canCreateInventario(): boolean {
    return this.permissionService.canAction('inventarios', 'create');
  }

  get canEditInventario(): boolean {
    return this.permissionService.canAction('inventarios', 'edit');
  }

  get canDeleteInventario(): boolean {
    return this.permissionService.canAction('inventarios', 'delete');
  }

  get canActivateInventario(): boolean {
    return this.permissionService.canAction('inventarios', 'activate');
  }

  get canDeactivateInventario(): boolean {
    return this.permissionService.canAction('inventarios', 'deactivate');
  }

  ngOnInit(): void {
    this.cargarInventarios();
    this.searchSubject
      .pipe(debounceTime(300), distinctUntilChanged(), takeUntil(this.destroy$))
      .subscribe(searchTerm => {
        this.filtros.search = searchTerm;
        this.cargarInventarios();
      });
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }

  cargarInventarios(): void {
    this.loading = true;
    this.filtros.pageNumber = this.paginacion.page;
    this.filtros.pageSize = this.paginacion.limit;

    if (this.selectedEstado === 'active') {
      this.filtros.estado = true;
    } else if (this.selectedEstado === 'inactive') {
      this.filtros.estado = false;
    } else {
      this.filtros.estado = undefined;
    }

    this.inventarioService.getInventarios(this.filtros).subscribe({
      next: (response: any) => {
        this.inventarios = response.data;
        this.paginacion.total = response.totalCount;
        this.loading = false;
        this.error = null;
      },
      error: () => {
        this.error = 'Error al cargar inventarios';
        this.loading = false;
      },
    });
  }

  toggleFilterMenu(): void {
    this.showFilterMenu = !this.showFilterMenu;
  }

  selectEstado(estado: string): void {
    this.selectedEstado = estado;
    this.showFilterMenu = false;
    this.paginacion.page = 1;
    this.cargarInventarios();
  }

  onSearch(): void {
    this.searchSubject.next(this.searchTerm);
  }

  onSearchKeyPress(event: KeyboardEvent): void {
    if (event.key === 'Enter') this.onSearch();
  }

  cambiarPagina(nuevaPagina: number): void {
    this.paginacion.page = nuevaPagina;
    this.cargarInventarios();
  }

  get totalPaginas(): number {
    return Math.ceil(this.paginacion.total / this.paginacion.limit);
  }

  previousPage(): void {
    if (this.paginacion.page > 1) {
      this.cambiarPagina(this.paginacion.page - 1);
    }
  }

  nextPage(): void {
    if (this.paginacion.page < this.totalPaginas) {
      this.cambiarPagina(this.paginacion.page + 1);
    }
  }

  getPageNumbers(): number[] {
    return Array.from({ length: this.totalPaginas }, (_, i) => i + 1);
  }

  getFillerRows(): number[] {
    const count = Math.max(this.paginacion.limit - this.inventarios.length, 0);
    return Array(count).fill(0);
  }

  trackByInventarioId(_index: number, inv: Inventario): number {
    return inv.inventarioId;
  }

  selectPage(page: number): void {
    if (page < 1 || page > this.totalPaginas || page === this.paginacion.page)
      return;
    this.cambiarPagina(page);
  }

  getPageTabs(windowSize: number = 5): number[] {
    const total = this.totalPaginas;
    if (total <= windowSize) return this.getPageNumbers();
    let start = Math.max(1, this.paginacion.page - Math.floor(windowSize / 2));
    const end = Math.min(total, start + windowSize - 1);
    if (end - start + 1 < windowSize) start = Math.max(1, end - windowSize + 1);
    return Array.from({ length: end - start + 1 }, (_, i) => start + i);
  }

  abrirModalCrear(): void {
    if (
      !this.permissionService.guardAction(
        'inventarios',
        'create',
        'No tienes permisos para crear inventarios.'
      )
    ) {
      return;
    }

    this.inventarioSeleccionado = null;
    this.modalMode = 'create';
    this.showModal = true;
  }

  abrirModalEditar(inventario: Inventario): void {
    if (
      !this.permissionService.guardAction(
        'inventarios',
        'edit',
        'No tienes permisos para editar inventarios.'
      )
    ) {
      return;
    }

    this.inventarioSeleccionado = inventario;
    this.modalMode = 'edit';
    this.showModal = true;
  }

  abrirModalVer(inventario: Inventario): void {
    this.inventarioSeleccionado = inventario;
    this.modalMode = 'view';
    this.showModal = true;
  }

  onInventarioGuardado(): void {
    this.cargarInventarios();
    this.notificationService.refreshInventoryAlerts();
    this.showModal = false;
  }

  onModalCerrado(): void {
    this.showModal = false;
    this.inventarioSeleccionado = null;
  }

  // Reorden encapsulado en la campana

  confirmarEliminar(inventario: Inventario): void {
    if (
      !this.permissionService.guardAction(
        'inventarios',
        'delete',
        'No tienes permisos para eliminar inventarios.'
      )
    ) {
      return;
    }

    this.inventarioAEliminar = inventario;
    this.showDeleteConfirm = true;
  }

  eliminarInventario(): void {
    if (
      !this.permissionService.guardAction(
        'inventarios',
        'delete',
        'No tienes permisos para eliminar inventarios.'
      )
    ) {
      return;
    }

    if (!this.inventarioAEliminar) return;
    this.inventarioService
      .deleteInventario(this.inventarioAEliminar.inventarioId)
      .subscribe({
        next: () => {
          this.cargarInventarios();
          this.notificationService.refreshInventoryAlerts();
          this.showDeleteConfirm = false;
          this.inventarioAEliminar = null;
        },
        error: () => {
          this.error = 'Error al eliminar inventario';
          this.showDeleteConfirm = false;
        },
      });
  }

  cancelarEliminar(): void {
    this.showDeleteConfirm = false;
    this.inventarioAEliminar = null;
  }

  toggleEstado(inventario: Inventario): void {
    const action = inventario.estado ? 'deactivate' : 'activate';
    const deniedMessage = inventario.estado
      ? 'No tienes permisos para desactivar inventarios.'
      : 'No tienes permisos para activar inventarios.';

    if (!this.permissionService.guardAction('inventarios', action, deniedMessage)) {
      return;
    }

    if (inventario.estado) {
      // Si está activo y se va a desactivar (dar de baja), pedir confirmación
      this.inventarioADesactivar = inventario;
      this.showDeactivateConfirm = true;
    } else {
      // Si está inactivo y se va a activar, proceder directamente
      this.procederToggleEstado(inventario);
    }
  }

  procederToggleEstado(inventario: Inventario): void {
    const action = inventario.estado ? 'deactivate' : 'activate';
    const deniedMessage = inventario.estado
      ? 'No tienes permisos para desactivar inventarios.'
      : 'No tienes permisos para activar inventarios.';

    if (!this.permissionService.guardAction('inventarios', action, deniedMessage)) {
      this.showDeactivateConfirm = false;
      this.inventarioADesactivar = null;
      return;
    }

    const accion = inventario.estado
      ? this.inventarioService.deactivateInventario(inventario.inventarioId)
      : this.inventarioService.activateInventario(inventario.inventarioId);
    accion.subscribe({
      next: () => {
        this.cargarInventarios();
        this.notificationService.refreshInventoryAlerts();
        this.showDeactivateConfirm = false;
        this.inventarioADesactivar = null;
      },
      error: () => {
        this.error = 'Error al cambiar estado';
        this.showDeactivateConfirm = false;
      },
    });
  }

  cancelarDeactivar(): void {
    this.showDeactivateConfirm = false;
    this.inventarioADesactivar = null;
  }
}
