import { Component, OnInit, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Subject, takeUntil, debounceTime, distinctUntilChanged } from 'rxjs';
import { ProveedorService } from '../../services/proveedores.service';
import { Proveedor, ProveedorFilters } from '../../models/proveedor.models';
import { ProveedoresModalComponent } from '../proveedores-modal/proveedores-modal.component';
import { SidebarComponent } from '@shared/ui/layout/sidebar/sidebar.component';
import { BellNotificationComponent } from '@shared/ui/notifications/bell-notification.component';
import { PermissionService } from '@core/auth/permission.service';

@Component({
  selector: 'app-proveedores',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    ProveedoresModalComponent,
    SidebarComponent,
    BellNotificationComponent,
  ],
  templateUrl: './proveedores.component.html',
  styleUrl: './proveedores.component.css',
})
export class ProveedoresComponent implements OnInit, OnDestroy {
  private destroy$ = new Subject<void>();
  private searchSubject = new Subject<string>();

  proveedores: Proveedor[] = [];
  filtros: ProveedorFilters = {};
  paginacion = {
    page: 1,
    limit: 10,
    total: 0,
  };
  loading = false;
  error: string | null = null;
  searchTerm = '';
  selectedEstado: string = 'all';
  showFilterMenu = false;
  selectedProveedor: Proveedor | null = null;
  showModal = false;
  modalMode: 'create' | 'edit' | 'view' = 'create';
  proveedorSeleccionado: Proveedor | null = null;
  showDeleteConfirm = false;
  showDeleteSuccess = false;
  proveedorAEliminar: Proveedor | null = null;
  showDeactivateConfirm = false;
  showDeactivateSuccess = false;
  proveedorADarDeBaja: Proveedor | null = null;

  // Referencia a Math para usar en el template
  Math = Math;

  constructor(
    private proveedorService: ProveedorService,
    private permissionService: PermissionService
  ) {}

  get canCreateProveedor(): boolean {
    return this.permissionService.canAction('proveedores', 'create');
  }

  get canEditProveedor(): boolean {
    return this.permissionService.canAction('proveedores', 'edit');
  }

  get canDeleteProveedor(): boolean {
    return this.permissionService.canAction('proveedores', 'delete');
  }

  get canDeactivateProveedor(): boolean {
    return this.permissionService.canAction('proveedores', 'deactivate');
  }

  ngOnInit(): void {
    this.cargarProveedores();
    this.searchSubject
      .pipe(debounceTime(300), distinctUntilChanged(), takeUntil(this.destroy$))
      .subscribe(searchTerm => {
        this.filtros.search = searchTerm;
        this.cargarProveedores();
      });
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }

  cargarProveedores(): void {
    this.loading = true;

    if (this.selectedEstado === 'active') {
      this.filtros.activo = true;
    } else if (this.selectedEstado === 'inactive') {
      this.filtros.activo = false;
    } else {
      this.filtros.activo = undefined;
    }

    this.proveedorService
      .getProveedores(this.paginacion.page, this.paginacion.limit, this.filtros)
      .subscribe({
        next: (response: any) => {
          this.proveedores = response.data;
          this.paginacion.total = response.totalCount;
          this.loading = false;
          this.error = null;
        },
        error: () => {
          this.error =
            'Error al cargar los proveedores. Por favor, inténtelo de nuevo.';
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
    this.cargarProveedores();
  }

  // --- Métodos para búsqueda ---
  onSearch(): void {
    this.searchSubject.next(this.searchTerm);
  }

  onSearchKeyPress(event: KeyboardEvent): void {
    if (event.key === 'Enter') {
      this.onSearch();
    }
  }

  onSearchEvent(event: Event): void {
    const value = (event.target as HTMLInputElement).value;
    this.searchTerm = value;
    this.onSearch();
  }

  getNoDataMessage(): string {
    return 'No se encontraron proveedores';
  }

  // --- Métodos para paginación ---
  get totalPages(): number {
    return Math.ceil(this.paginacion.total / this.paginacion.limit) || 1;
  }

  previousPage(): void {
    if (this.paginacion.page > 1) {
      this.paginacion.page--;
      this.cargarProveedores();
    }
  }

  nextPage(): void {
    if (this.paginacion.page < this.totalPages) {
      this.paginacion.page++;
      this.cargarProveedores();
    }
  }

  getPageNumbers(): number[] {
    return Array.from({ length: this.totalPages }, (_, i) => i + 1);
  }

  changePage(page: number): void {
    this.paginacion.page = page;
    this.cargarProveedores();
  }

  // --- Modal ---
  crearProveedor(): void {
    if (
      !this.permissionService.guardAction(
        'proveedores',
        'create',
        'No tienes permisos para crear proveedores.'
      )
    ) {
      return;
    }

    this.selectedProveedor = null;
    this.modalMode = 'create';
    this.showModal = true;
  }

  editarProveedor(proveedor: Proveedor): void {
    if (
      !this.permissionService.guardAction(
        'proveedores',
        'edit',
        'No tienes permisos para editar proveedores.'
      )
    ) {
      return;
    }

    this.selectedProveedor = proveedor;
    this.modalMode = 'edit';
    this.showModal = true;
  }

  verProveedor(proveedor: Proveedor): void {
    this.selectedProveedor = proveedor;
    this.modalMode = 'view';
    this.showModal = true;
  }

  onModalSuccess(proveedor: Proveedor): void {
    console.log('Proveedor guardado:', proveedor);
    this.showModal = false;
    this.cargarProveedores();
  }

  onModalClosed(): void {
    this.showModal = false;
    this.selectedProveedor = null;
  }

  closeModal(): void {
    this.showModal = false;
  }

  /**
   * Abre el modal de confirmación para eliminar proveedor
   */
  eliminarProveedor(proveedor: Proveedor): void {
    if (
      !this.permissionService.guardAction(
        'proveedores',
        'delete',
        'No tienes permisos para eliminar proveedores.'
      )
    ) {
      return;
    }

    this.proveedorAEliminar = proveedor;
    this.showDeleteConfirm = true;
  }

  /**
   * Confirma la eliminación del proveedor
   */
  confirmarEliminacion(): void {
    if (
      !this.permissionService.guardAction(
        'proveedores',
        'delete',
        'No tienes permisos para eliminar proveedores.'
      )
    ) {
      return;
    }

    if (!this.proveedorAEliminar) return;

    this.loading = true;
    this.proveedorService
      .deleteProveedor(this.proveedorAEliminar.proveedor_id)
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: () => {
          this.loading = false;
          this.showDeleteConfirm = false;
          this.showDeleteSuccess = true;
          this.proveedorAEliminar = null;
          this.cargarProveedores(); // Recargar la lista
        },
        error: error => {
          console.error('Error al eliminar proveedor:', error);
          this.loading = false;
          this.error =
            'Error al eliminar el proveedor. Por favor, intente nuevamente.';
          this.showDeleteConfirm = false;
        },
      });
  }

  /**
   * Cancela la eliminación del proveedor
   */
  cancelarEliminacion(): void {
    this.showDeleteConfirm = false;
    this.proveedorAEliminar = null;
  }

  /**
   * Cierra el modal de éxito de eliminación
   */
  cerrarModalEliminacion(): void {
    this.showDeleteSuccess = false;
    this.proveedorAEliminar = null;
    this.cargarProveedores();
  }

  /**
   * Abre el modal de confirmación para dar de baja proveedor
   */
  darDeBajaProveedor(proveedor: Proveedor): void {
    if (
      !this.permissionService.guardAction(
        'proveedores',
        'deactivate',
        'No tienes permisos para desactivar proveedores.'
      )
    ) {
      return;
    }

    this.proveedorADarDeBaja = proveedor;
    this.showDeactivateConfirm = true;
  }

  /**
   * Confirma dar de baja al proveedor
   */
  confirmarDarDeBaja(): void {
    if (
      !this.permissionService.guardAction(
        'proveedores',
        'deactivate',
        'No tienes permisos para desactivar proveedores.'
      )
    ) {
      return;
    }

    if (!this.proveedorADarDeBaja) return;

    this.loading = true;
    this.proveedorService
      .deactivateProveedor(this.proveedorADarDeBaja.proveedor_id)
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: () => {
          this.loading = false;
          this.showDeactivateConfirm = false;
          this.showDeactivateSuccess = true;
          this.proveedorADarDeBaja = null; // Limpiar la referencia
          this.cargarProveedores(); // Recargar la lista
        },
        error: (error: any) => {
          console.error('Error al dar de baja proveedor:', error);
          this.loading = false;
          this.error =
            'Error al dar de baja el proveedor. Por favor, intente nuevamente.';
          this.showDeactivateConfirm = false;
          this.proveedorADarDeBaja = null; // Limpiar la referencia también en caso de error
        },
      });
  }

  /**
   * Cancela dar de baja al proveedor
   */
  cancelarDarDeBaja(): void {
    this.showDeactivateConfirm = false;
    this.proveedorADarDeBaja = null;
  }

  /**
   * Cierra el modal de éxito de dar de baja
   */
  cerrarModalDarDeBaja(): void {
    this.showDeactivateSuccess = false;
    this.proveedorADarDeBaja = null;
    this.cargarProveedores();
  }

  /**
   * Maneja clics en el backdrop de los modales
   */
  onBackdropClick(event: Event): void {
    if (event.target === event.currentTarget) {
      if (this.showDeleteConfirm) {
        this.cancelarEliminacion();
      } else if (this.showDeleteSuccess) {
        this.cerrarModalEliminacion();
      } else if (this.showDeactivateConfirm) {
        this.cancelarDarDeBaja();
      } else if (this.showDeactivateSuccess) {
        this.cerrarModalDarDeBaja();
      }
    }
  }

  trackByProveedorId(index: number, proveedor: Proveedor): number {
    return proveedor.proveedor_id;
  }

  getInitials(nombre: string): string {
    if (!nombre) return '';
    return nombre
      .split(' ')
      .map(n => n[0])
      .slice(0, 2)
      .join('')
      .toUpperCase();
  }

  toggleDarkMode(): void {
    document.documentElement.classList.toggle('dark');
  }

  getFillerRows(): number[] {
    const fillerCount = Math.max(
      0,
      this.paginacion.limit - this.proveedores.length
    );
    return Array(fillerCount)
      .fill(0)
      .map((_, i) => i);
  }
}
