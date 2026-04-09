import { Component, OnInit, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Subject, debounceTime, distinctUntilChanged, takeUntil } from 'rxjs';

import { TareaService } from '../../services/tareas.service';
import { Tarea, TareaFilters } from '../../models/tarea.models';
import { TareasModalComponent } from '../tareas-modal/tareas-modal.component';
import { SidebarComponent } from '../../../../shared/ui/layout/sidebar/sidebar.component';
import { BellNotificationComponent } from '@shared/ui/notifications/bell-notification.component';
import { PermissionService } from '@core/auth/permission.service';

@Component({
  selector: 'app-tareas-view',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    TareasModalComponent,
    SidebarComponent,
    BellNotificationComponent,
  ],
  templateUrl: './tareas-view.component.html',
  styleUrls: ['./tareas-view.component.css'],
})
export class TareasViewComponent implements OnInit, OnDestroy {
  private destroy$ = new Subject<void>();
  private search$ = new Subject<string>();

  tareas: Tarea[] = [];
  filtros: TareaFilters = {};
  paginacion = { page: 1, limit: 10, total: 0 };
  loading = false;
  searchTerm = '';
  selectedEstado: string = 'all';
  showFilterMenu = false;

  selectedTarea: Tarea | null = null;
  showModal = false;
  modalMode: 'create' | 'edit' | 'view' = 'create';

  showDeleteConfirm = false;
  showDeleteSuccess = false;
  tareaAEliminar: Tarea | null = null;

  showDeactivateConfirm = false;
  showDeactivateSuccess = false;
  tareaADarDeBaja: Tarea | null = null;

  showActivateConfirm = false;
  showActivateSuccess = false;
  tareaAActivar: Tarea | null = null;

  Math = Math;

  constructor(
    private tareaService: TareaService,
    private permissionService: PermissionService
  ) {}

  get canCreateTarea(): boolean {
    return this.permissionService.canAction('tareas', 'create');
  }

  get canEditTarea(): boolean {
    return this.permissionService.canAction('tareas', 'edit');
  }

  get canDeleteTarea(): boolean {
    return this.permissionService.canAction('tareas', 'delete');
  }

  get canDeactivateTarea(): boolean {
    return this.permissionService.canAction('tareas', 'deactivate');
  }

  get canActivateTarea(): boolean {
    return this.permissionService.canAction('tareas', 'activate');
  }

  toggleDarkMode(): void {
    document.documentElement.classList.toggle('dark');
  }

  toggleFilterMenu(): void {
    this.showFilterMenu = !this.showFilterMenu;
  }

  selectEstado(estado: string): void {
    this.selectedEstado = estado;
    this.showFilterMenu = false;

    if (this.selectedEstado === 'active') {
      this.filtros.estado = true;
    } else if (this.selectedEstado === 'inactive') {
      this.filtros.estado = false;
    } else {
      this.filtros.estado = undefined;
    }

    this.paginacion.page = 1;
    this.cargar();
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
    this.tareaService
      .getTareas(this.paginacion.page, this.paginacion.limit, this.filtros)
      .subscribe({
        next: (res: any) => {
          this.tareas = res.data;
          this.paginacion.total = res.totalCount;
          this.loading = false;
        },
        error: () => (this.loading = false),
      });
  }

  crearTarea(): void {
    if (
      !this.permissionService.guardAction(
        'tareas',
        'create',
        'No tienes permisos para crear tareas.'
      )
    ) {
      return;
    }

    this.selectedTarea = null;
    this.modalMode = 'create';
    this.showModal = true;
  }

  editar(t: Tarea): void {
    if (
      !this.permissionService.guardAction(
        'tareas',
        'edit',
        'No tienes permisos para editar tareas.'
      )
    ) {
      return;
    }

    this.selectedTarea = t;
    this.modalMode = 'edit';
    this.showModal = true;
  }

  onModalSuccess(): void {
    this.showModal = false;
    this.cargar();
  }

  closeModal(): void {
    this.showModal = false;
  }

  eliminar(t: Tarea): void {
    if (
      !this.permissionService.guardAction(
        'tareas',
        'delete',
        'No tienes permisos para eliminar tareas.'
      )
    ) {
      return;
    }

    this.tareaAEliminar = t;
    this.showDeleteConfirm = true;
  }

  confirmarEliminacion(): void {
    if (
      !this.permissionService.guardAction(
        'tareas',
        'delete',
        'No tienes permisos para eliminar tareas.'
      )
    ) {
      return;
    }

    if (!this.tareaAEliminar) return;
    this.loading = true;
    this.tareaService.deleteTarea(this.tareaAEliminar.tareaId).subscribe({
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
    this.tareaAEliminar = null;
  }

  cerrarModalEliminacion(): void {
    this.showDeleteSuccess = false;
  }

  darDeBaja(t: Tarea): void {
    if (
      !this.permissionService.guardAction(
        'tareas',
        'deactivate',
        'No tienes permisos para desactivar tareas.'
      )
    ) {
      return;
    }

    this.tareaADarDeBaja = t;
    this.showDeactivateConfirm = true;
  }

  confirmarDarDeBaja(): void {
    if (
      !this.permissionService.guardAction(
        'tareas',
        'deactivate',
        'No tienes permisos para desactivar tareas.'
      )
    ) {
      return;
    }

    if (!this.tareaADarDeBaja) return;
    this.loading = true;
    this.tareaService.deactivateTarea(this.tareaADarDeBaja.tareaId).subscribe({
      next: () => {
        this.loading = false;
        this.showDeactivateConfirm = false;
        this.showDeactivateSuccess = true;
        this.tareas = this.tareas.filter(
          t => t.tareaId !== this.tareaADarDeBaja?.tareaId
        );
        this.cargar();
      },
      error: () => {
        this.loading = false;
        this.showDeactivateConfirm = false;
      },
    });
  }

  cancelarDarDeBaja(): void {
    this.showDeactivateConfirm = false;
    this.tareaADarDeBaja = null;
  }

  cerrarModalDarDeBaja(): void {
    this.showDeactivateSuccess = false;
  }

  activar(t: Tarea): void {
    if (
      !this.permissionService.guardAction(
        'tareas',
        'activate',
        'No tienes permisos para activar tareas.'
      )
    ) {
      return;
    }

    this.tareaAActivar = t;
    this.showActivateConfirm = true;
  }

  confirmarActivacion(): void {
    if (
      !this.permissionService.guardAction(
        'tareas',
        'activate',
        'No tienes permisos para activar tareas.'
      )
    ) {
      return;
    }

    if (!this.tareaAActivar) return;

    this.loading = true;
    this.tareaService.activateTarea(this.tareaAActivar.tareaId).subscribe({
      next: () => {
        this.loading = false;
        this.showActivateConfirm = false;
        this.showActivateSuccess = true;
        this.cargar();
      },
      error: () => {
        this.loading = false;
        this.showActivateConfirm = false;
      },
    });
  }

  cancelarActivacion(): void {
    this.showActivateConfirm = false;
    this.tareaAActivar = null;
  }

  cerrarModalActivacion(): void {
    this.showActivateSuccess = false;
  }

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

  trackById(i: number, t: Tarea): number {
    return t.tareaId;
  }

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
    return Array(Math.max(0, this.paginacion.limit - this.tareas.length))
      .fill(0)
      .map((_, i) => i);
  }

  getNoDataMessage(): string {
    return 'No se encontraron tareas';
  }

  onBackdropClick(event: Event): void {
    if (event.target === event.currentTarget) {
      if (this.showDeleteConfirm) this.cancelarEliminacion();
      else if (this.showDeleteSuccess) this.cerrarModalEliminacion();
      else if (this.showDeactivateConfirm) this.cancelarDarDeBaja();
      else if (this.showDeactivateSuccess) this.cerrarModalDarDeBaja();
      else if (this.showActivateConfirm) this.cancelarActivacion();
      else if (this.showActivateSuccess) this.cerrarModalActivacion();
    }
  }
}
