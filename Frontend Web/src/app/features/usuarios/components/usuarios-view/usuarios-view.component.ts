import { Component, OnInit, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Subject, debounceTime, distinctUntilChanged, takeUntil } from 'rxjs';

import { UsuarioService } from '../../services/usuarios.service';
import { Usuario, UsuarioFilters } from '../../models/usuario.models';
import { UsuariosModalComponent } from '../usuarios-modal/usuarios-modal.component';
import { SidebarComponent } from '../../../../shared/ui/layout/sidebar/sidebar.component';
import { BellNotificationComponent } from '@shared/ui/notifications/bell-notification.component';
import { PermissionService } from '@core/auth/permission.service';

@Component({
  selector: 'app-usuarios-view',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    UsuariosModalComponent,
    SidebarComponent,
    BellNotificationComponent,
  ],
  templateUrl: './usuarios-view.component.html',
  styleUrls: ['./usuarios-view.component.css'],
})
export class UsuariosViewComponent implements OnInit, OnDestroy {
  private destroy$ = new Subject<void>();
  private search$ = new Subject<string>();

  usuarios: Usuario[] = [];
  filtros: UsuarioFilters = {};
  paginacion = { page: 1, limit: 10, total: 0 };
  loading = false;
  searchTerm = '';
  selectedEstado: string = 'all';
  showFilterMenu = false;

  selectedUsuario: Usuario | null = null;
  showModal = false;
  modalMode: 'create' | 'edit' | 'view' = 'create';

  showDeleteConfirm = false;
  showDeleteSuccess = false;
  usuarioAEliminar: Usuario | null = null;

  showDeactivateConfirm = false;
  showDeactivateSuccess = false;
  usuarioADarDeBaja: Usuario | null = null;

  usuarioAActivar: Usuario | null = null;
  showActivateConfirm = false;
  showActivateSuccess = false;

  showErrorAlert = false;
  errorMessage = '';

  Math = Math;

  constructor(
    private usuarioService: UsuarioService,
    private permissionService: PermissionService
  ) {}

  get canCreateUsuario(): boolean {
    return this.permissionService.canAction('usuarios', 'create');
  }

  get canEditUsuario(): boolean {
    return this.permissionService.canAction('usuarios', 'edit');
  }

  get canDeleteUsuario(): boolean {
    return this.permissionService.canAction('usuarios', 'delete');
  }

  get canDeactivateUsuario(): boolean {
    return this.permissionService.canAction('usuarios', 'deactivate');
  }

  get canActivateUsuario(): boolean {
    return this.permissionService.canAction('usuarios', 'activate');
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
    
    // Reset role filters
    this.filtros.roleNames = undefined;

    // Ensure filters are synced with selection
    if (this.selectedEstado === 'active') {
      this.filtros.estado = true;
    } else if (this.selectedEstado === 'inactive') {
      this.filtros.estado = false;
    } else if (this.selectedEstado === 'operarios') {
      this.filtros.estado = undefined; // Opcional: ¿los operarios deben ser activos? Por defecto trae todos
      this.filtros.roleNames = ['Administrador', 'Auxiliar', 'Supervisor'];
    } else {
      this.filtros.estado = undefined;
    }

    this.usuarioService
      .getUsuarios(this.paginacion.page, this.paginacion.limit, this.filtros)
      .subscribe({
        next: (res: any) => {
          this.usuarios = res.data;
          this.paginacion.total = res.totalCount;
          this.loading = false;
        },
        error: () => (this.loading = false),
      });
  }

  onEstadoChange(): void {
    this.paginacion.page = 1;
    this.cargar();
  }

  toggleFilterMenu(): void {
    this.showFilterMenu = !this.showFilterMenu;
  }

  selectEstado(estado: string): void {
    this.selectedEstado = estado;
    this.showFilterMenu = false;
    this.onEstadoChange();
  }

  crearUsuario(): void {
    if (
      !this.permissionService.guardAction(
        'usuarios',
        'create',
        'No tienes permisos para crear usuarios.'
      )
    ) {
      return;
    }

    this.selectedUsuario = null;
    this.modalMode = 'create';
    this.showModal = true;
  }
  editar(_u: Usuario): void {
    if (
      !this.permissionService.guardAction(
        'usuarios',
        'edit',
        'No tienes permisos para editar usuarios.'
      )
    ) {
      return;
    }

    this.selectedUsuario = _u;
    this.modalMode = 'edit';
    this.showModal = true;
  }

  onModalSuccess(_u: Usuario): void {
    this.showModal = false;
    this.cargar();
  }
  closeModal(): void {
    this.showModal = false;
  }

  eliminar(u: Usuario): void {
    if (
      !this.permissionService.guardAction(
        'usuarios',
        'delete',
        'No tienes permisos para eliminar usuarios.'
      )
    ) {
      return;
    }

    this.usuarioAEliminar = u;
    this.showDeleteConfirm = true;
  }

  confirmarEliminacion(): void {
    if (
      !this.permissionService.guardAction(
        'usuarios',
        'delete',
        'No tienes permisos para eliminar usuarios.'
      )
    ) {
      return;
    }

    if (!this.usuarioAEliminar) return;
    this.loading = true;
    this.usuarioService
      .deleteUsuario(this.usuarioAEliminar.usuario_id)
      .subscribe({
        next: () => {
          this.loading = false;
          this.showDeleteConfirm = false;
          this.showDeleteSuccess = true;
          this.cargar();
        },
        error: (err: any) => {
          this.loading = false;
          this.showDeleteConfirm = false;
          
          // Personalizar mensaje de error si el backend devuelve un 400 por tareas vinculadas
          if (err.status === 400 || err.error?.message?.toLowerCase().includes('tarea')) {
            this.errorMessage = 'No se puede eliminar el usuario porque tiene tareas vinculadas. Por favor, reasigne o elimine las tareas antes de continuar.';
          } else {
            this.errorMessage = err.error?.message || 'Error al eliminar el usuario';
          }
          
          this.showErrorAlert = true;
        },
      });
  }

  cancelarEliminacion(): void {
    this.showDeleteConfirm = false;
    this.usuarioAEliminar = null;
  }
  cerrarModalEliminacion(): void {
    this.showDeleteSuccess = false;
  }

  // --- DAR DE BAJA ---
  darDeBaja(u: Usuario): void {
    if (
      !this.permissionService.guardAction(
        'usuarios',
        'deactivate',
        'No tienes permisos para desactivar usuarios.'
      )
    ) {
      return;
    }

    this.usuarioADarDeBaja = u;
    this.showDeactivateConfirm = true;
  }

  confirmarDarDeBaja(): void {
    if (
      !this.permissionService.guardAction(
        'usuarios',
        'deactivate',
        'No tienes permisos para desactivar usuarios.'
      )
    ) {
      return;
    }

    if (!this.usuarioADarDeBaja) return;

    this.loading = true;

    this.usuarioService
      .deactivateUsuario(this.usuarioADarDeBaja.usuario_id)
      .subscribe({
        next: () => {
          this.loading = false;
          this.showDeactivateConfirm = false;
          this.showDeactivateSuccess = true;

          // 👇 ESTO HACE QUE DESAPAREZCA AL INSTANTE 👇
          // Filtramos la lista para quitar al usuario que acabamos de desactivar
          this.usuarios = this.usuarios.filter(
            u => u.usuario_id !== this.usuarioADarDeBaja?.usuario_id
          );

          // Y luego recargamos (el servicio ya no lo traerá)
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
    this.usuarioADarDeBaja = null;
  }
  cerrarModalDarDeBaja(): void {
    this.showDeactivateSuccess = false;
  }

  // --- ACTIVAR ---
  activar(u: Usuario): void {
    if (
      !this.permissionService.guardAction(
        'usuarios',
        'activate',
        'No tienes permisos para activar usuarios.'
      )
    ) {
      return;
    }

    this.usuarioAActivar = u;
    this.showActivateConfirm = true;
  }

  confirmarActivar(): void {
    if (
      !this.permissionService.guardAction(
        'usuarios',
        'activate',
        'No tienes permisos para activar usuarios.'
      )
    ) {
      return;
    }

    if (!this.usuarioAActivar) return;

    this.loading = true;

    this.usuarioService.activateUsuario(this.usuarioAActivar.usuario_id).subscribe({
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

  cancelarActivar(): void {
    this.showActivateConfirm = false;
    this.usuarioAActivar = null;
  }
  cerrarModalActivar(): void {
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

  trackById(i: number, u: Usuario): number {
    return u.usuario_id;
  }

  getInitials(name: string): string {
    if (!name) return '';
    return name
      .split(' ')
      .map(n => n[0])
      .slice(0, 2)
      .join('')
      .toUpperCase();
  }

  toggleDarkMode(): void {
    document.documentElement.classList.toggle('dark');
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
    return Array(Math.max(0, this.paginacion.limit - this.usuarios.length))
      .fill(0)
      .map((_, i) => i);
  }
  getNoDataMessage(): string {
    return 'No se encontraron usuarios';
  }

  onBackdropClick(event: Event): void {
    if (event.target === event.currentTarget) {
      if (this.showDeleteConfirm) this.cancelarEliminacion();
      else if (this.showDeleteSuccess) this.cerrarModalEliminacion();
      else if (this.showDeactivateConfirm) this.cancelarDarDeBaja();
      else if (this.showDeactivateSuccess) this.cerrarModalDarDeBaja();
    }
  }
}
