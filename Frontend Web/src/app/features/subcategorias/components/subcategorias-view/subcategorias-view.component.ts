import { Component, OnInit, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Subject, takeUntil, debounceTime, distinctUntilChanged } from 'rxjs';
// CAMBIOS: Importaciones actualizadas para Subcategoria
import { SubcategoriaService } from '../../services/subcategorias.service';
import {
  Subcategoria,
  SubcategoriaFilters,
} from '../../models/subcategoria.models';
import { SubcategoriasModalComponent } from '../subcategorias-model/subcategorias-modal.component';
import { SidebarComponent } from '@shared/ui/layout/sidebar/sidebar.component';
import { BellNotificationComponent } from '@shared/ui/notifications/bell-notification.component';
import { PermissionService } from '@core/auth/permission.service';

@Component({
  // CAMBIO: Selector y archivos actualizados
  selector: 'app-subcategorias-view',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    SubcategoriasModalComponent, // CAMBIO
    SidebarComponent,
    BellNotificationComponent,
  ],
  templateUrl: './subcategorias-view.component.html',
  styleUrls: ['./subcategorias-view.component.css'],
})
// CAMBIO: Nombre de la clase
export class SubcategoriasViewComponent implements OnInit, OnDestroy {
  private destroy$ = new Subject<void>();
  private searchSubject = new Subject<string>();

  // CAMBIO: Nombres de variables
  subcategorias: Subcategoria[] = [];
  filtros: SubcategoriaFilters = {};
  paginacion = {
    page: 1,
    limit: 10,
    total: 0,
  };
  loading = false;
  error: string | null = null;
  searchTerm = '';
  selectedSubcategoria: Subcategoria | null = null;
  showFilterMenu = false;
  selectedEstado: string = 'all';
  showModal = false;
  modalMode: 'create' | 'edit' | 'view' = 'create';
  subcategoriaSeleccionada: Subcategoria | null = null;
  showDeleteConfirm = false;
  showDeleteSuccess = false;
  subcategoriaAEliminar: Subcategoria | null = null;
  showDeactivateConfirm = false;
  showDeactivateSuccess = false;
  subcategoriaADarDeBaja: Subcategoria | null = null;

  Math = Math;

  // CAMBIO: Inyección del nuevo servicio
  constructor(
    private subcategoriaService: SubcategoriaService,
    private permissionService: PermissionService
  ) {}

  get canCreateSubcategoria(): boolean {
    return this.permissionService.canAction('subcategorias', 'create');
  }

  get canEditSubcategoria(): boolean {
    return this.permissionService.canAction('subcategorias', 'edit');
  }

  get canDeleteSubcategoria(): boolean {
    return this.permissionService.canAction('subcategorias', 'delete');
  }

  get canActivateSubcategoria(): boolean {
    return this.permissionService.canAction('subcategorias', 'activate');
  }

  get canDeactivateSubcategoria(): boolean {
    return this.permissionService.canAction('subcategorias', 'deactivate');
  }

  ngOnInit(): void {
    this.cargarSubcategorias();
    this.searchSubject
      .pipe(debounceTime(300), distinctUntilChanged(), takeUntil(this.destroy$))
      .subscribe(searchTerm => {
        this.filtros.search = searchTerm;
        this.cargarSubcategorias();
      });
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }

  // CAMBIO: Lógica para cargar subcategorías
  cargarSubcategorias(): void {
    this.loading = true;

    if (this.selectedEstado === 'active') {
      this.filtros.estado = true;
    } else if (this.selectedEstado === 'inactive') {
      this.filtros.estado = false;
    } else {
      this.filtros.estado = undefined;
    }

    this.subcategoriaService
      .getSubcategorias(
        this.paginacion.page,
        this.paginacion.limit,
        this.filtros
      )
      .subscribe({
        next: (response: any) => {
          this.subcategorias = response.data;
          this.paginacion.total = response.totalCount;
          this.loading = false;
          this.error = null;
        },
        error: () => {
          this.error =
            'Error al cargar las subcategorías. Por favor, inténtelo de nuevo.';
          this.loading = false;
        },
      });
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

  toggleFilterMenu(): void {
    this.showFilterMenu = !this.showFilterMenu;
  }

  selectEstado(estado: string): void {
    this.selectedEstado = estado;
    this.showFilterMenu = false;
    this.paginacion.page = 1;
    this.cargarSubcategorias();
  }

  getNoDataMessage(): string {
    return 'No se encontraron subcategorías';
  }

  // --- Métodos para paginación ---
  get totalPages(): number {
    return Math.ceil(this.paginacion.total / this.paginacion.limit) || 1;
  }

  previousPage(): void {
    if (this.paginacion.page > 1) {
      this.paginacion.page--;
      this.cargarSubcategorias();
    }
  }

  nextPage(): void {
    if (this.paginacion.page < this.totalPages) {
      this.paginacion.page++;
      this.cargarSubcategorias();
    }
  }

  getPageNumbers(): number[] {
    return Array.from({ length: this.totalPages }, (_, i) => i + 1);
  }

  changePage(page: number): void {
    this.paginacion.page = page;
    this.cargarSubcategorias();
  }

  // --- Modal ---
  crearSubcategoria(): void {
    if (
      !this.permissionService.guardAction(
        'subcategorias',
        'create',
        'No tienes permisos para crear subcategorias.'
      )
    ) {
      return;
    }

    this.selectedSubcategoria = null;
    this.modalMode = 'create';
    this.showModal = true;
  }

  editarSubcategoria(subcategoria: Subcategoria): void {
    if (
      !this.permissionService.guardAction(
        'subcategorias',
        'edit',
        'No tienes permisos para editar subcategorias.'
      )
    ) {
      return;
    }

    this.selectedSubcategoria = subcategoria;
    this.modalMode = 'edit';
    this.showModal = true;
  }

  verSubcategoria(subcategoria: Subcategoria): void {
    this.selectedSubcategoria = subcategoria;
    this.modalMode = 'view';
    this.showModal = true;
  }

  onModalSuccess(subcategoria: Subcategoria): void {
    console.log('Subcategoría guardada:', subcategoria);
    this.cargarSubcategorias();
  }

  onModalClosed(): void {
    this.showModal = false;
    this.selectedSubcategoria = null;
  }

  closeModal(): void {
    this.showModal = false;
  }

  eliminarSubcategoria(subcategoria: Subcategoria): void {
    if (
      !this.permissionService.guardAction(
        'subcategorias',
        'delete',
        'No tienes permisos para eliminar subcategorias.'
      )
    ) {
      return;
    }

    this.subcategoriaAEliminar = subcategoria;
    this.showDeleteConfirm = true;
  }

  confirmarEliminacion(): void {
    if (
      !this.permissionService.guardAction(
        'subcategorias',
        'delete',
        'No tienes permisos para eliminar subcategorias.'
      )
    ) {
      return;
    }

    if (!this.subcategoriaAEliminar) return;

    this.loading = true;
    this.subcategoriaService
      .deleteSubcategoria(this.subcategoriaAEliminar.subcategoriaId) // Asumiendo que el ID se llama así
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: () => {
          this.loading = false;
          this.showDeleteConfirm = false;
          this.showDeleteSuccess = true;
          this.subcategoriaAEliminar = null;
          this.cargarSubcategorias();
        },
        error: _error => {
          this.loading = false;
          this.error = 'Error al eliminar la subcategoría.';
          this.showDeleteConfirm = false;
        },
      });
  }

  cancelarEliminacion(): void {
    this.showDeleteConfirm = false;
    this.subcategoriaAEliminar = null;
  }

  cerrarModalEliminacion(): void {
    this.showDeleteSuccess = false;
    this.cargarSubcategorias();
  }

  activarSubcategoria(subcategoria: Subcategoria): void {
    if (
      !this.permissionService.guardAction(
        'subcategorias',
        'activate',
        'No tienes permisos para activar subcategorias.'
      )
    ) {
      return;
    }

    this.loading = true;
    this.subcategoriaService
      .activarSubcategoria(subcategoria.subcategoriaId)
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: () => {
          this.loading = false;
          this.cargarSubcategorias();
        },
        error: _error => {
          this.loading = false;
          this.error = 'Error al activar la subcategoría.';
        },
      });
  }

  desactivarSubcategoria(subcategoria: Subcategoria): void {
    if (
      !this.permissionService.guardAction(
        'subcategorias',
        'deactivate',
        'No tienes permisos para desactivar subcategorias.'
      )
    ) {
      return;
    }

    this.subcategoriaADarDeBaja = subcategoria;
    this.showDeactivateConfirm = true;
  }

  confirmarDarDeBaja(): void {
    if (
      !this.permissionService.guardAction(
        'subcategorias',
        'deactivate',
        'No tienes permisos para desactivar subcategorias.'
      )
    ) {
      return;
    }

    if (!this.subcategoriaADarDeBaja) return;

    this.loading = true;
    this.subcategoriaService
      .desactivarSubcategoria(this.subcategoriaADarDeBaja.subcategoriaId)
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: () => {
          this.loading = false;
          this.showDeactivateConfirm = false;
          this.showDeactivateSuccess = true;
          this.subcategoriaADarDeBaja = null;
          this.cargarSubcategorias();
        },
        error: _error => {
          this.loading = false;
          this.error = 'Error al dar de baja la subcategoría.';
          this.showDeactivateConfirm = false;
        },
      });
  }

  cancelarDarDeBaja(): void {
    this.showDeactivateConfirm = false;
    this.subcategoriaADarDeBaja = null;
  }

  cerrarModalDarDeBaja(): void {
    this.showDeactivateSuccess = false;
    this.cargarSubcategorias();
  }

  onBackdropClick(event: Event): void {
    if (event.target === event.currentTarget) {
      if (this.showDeleteConfirm) this.cancelarEliminacion();
      else if (this.showDeleteSuccess) this.cerrarModalEliminacion();
      else if (this.showDeactivateConfirm) this.cancelarDarDeBaja();
      else if (this.showDeactivateSuccess) this.cerrarModalDarDeBaja();
    }
  }

  toggleDarkMode(): void {
    document.documentElement.classList.toggle('dark');
  }

  trackBySubcategoriaId(index: number, subcategoria: Subcategoria): number {
    return subcategoria.subcategoriaId; // Asumiendo que el ID se llama así
  }

  getFillerRows(): number[] {
    const fillerCount = Math.max(
      0,
      this.paginacion.limit - this.subcategorias.length
    );
    return Array(fillerCount)
      .fill(0)
      .map((_, i) => i);
  }
}
