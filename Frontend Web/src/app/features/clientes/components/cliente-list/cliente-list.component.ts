import { Component, OnInit, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Subject, takeUntil, debounceTime, distinctUntilChanged } from 'rxjs';
import { ClienteService } from '@features/clientes/services/cliente.service';
import {
  Cliente,
  ClienteFilters,
  ClienteModalState,
} from '@features/clientes/models/cliente.models';
import { ClienteModalComponent } from '../cliente-modal/cliente-modal.component';
import { SidebarComponent } from '@shared/ui/layout/sidebar/sidebar.component';
import { BellNotificationComponent } from '@shared/ui/notifications/bell-notification.component';
import { PermissionService } from '@core/auth/permission.service';

@Component({
  selector: 'app-cliente-list',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    ClienteModalComponent,
    SidebarComponent,
    BellNotificationComponent,
  ],
  templateUrl: './cliente-list.component.html',
  styleUrl: './cliente-list.component.css',
})
export class ClienteListComponent implements OnInit, OnDestroy {
  private destroy$ = new Subject<void>();
  private searchSubject = new Subject<string>();

  // Propiedades del componente
  clientes: Cliente[] = [];
  filtros: ClienteFilters = {};
  paginacion = {
    page: 1,
    limit: 10,
    total: 0,
  };
  loading = false;
  error: string | null = null;
  searchTerm = '';
  selectedCliente: Cliente | null = null;
  showModal = false;
  modalMode: 'create' | 'edit' | 'view' = 'create';
  clienteSeleccionado: Cliente | null = null;
  showDeleteConfirm = false;
  showDeleteSuccess = false;
  clienteAEliminar: Cliente | null = null;
  showDeactivateConfirm = false;
  showDeactivateSuccess = false;
  clienteADarDeBaja: Cliente | null = null;
  
  showActivateConfirm = false;
  showActivateSuccess = false;
  clienteAActivar: Cliente | null = null;

  // Columnas para mostrar en la tabla
  displayedColumns = [
    'cliente_id',
    'nombre',
    'contacto',
    'telefono',
    'email',
    'acciones',
  ];

  // Referencia a Math para usar en el template
  Math = Math;

  // Estado del modal
  modalState: ClienteModalState = {
    isOpen: false,
    action: 'create',
  };

  // Filtros y búsqueda
  filters: ClienteFilters = {
    pageNumber: 1,
    pageSize: 10,
  };

  // Paginación
  totalClientes = 0;
  currentPage = 1;
  totalPages = 0;

  selectedEstado: string = 'all';
  showFilterMenu: boolean = false;

  constructor(
    private clienteService: ClienteService,
    private permissionService: PermissionService
  ) {
    // Configurar búsqueda con debounce
    this.searchSubject
      .pipe(debounceTime(300), distinctUntilChanged(), takeUntil(this.destroy$))
      .subscribe(searchTerm => {
        this.filters.search = searchTerm || undefined;
        this.loadClientes();
      });
  }

  get canCreateCliente(): boolean {
    return this.permissionService.canAction('clientes', 'create');
  }

  get canEditCliente(): boolean {
    return this.permissionService.canAction('clientes', 'edit');
  }

  get canDeleteCliente(): boolean {
    return this.permissionService.canAction('clientes', 'delete');
  }

  get canDeactivateCliente(): boolean {
    return this.permissionService.canAction('clientes', 'deactivate');
  }

  get canActivateCliente(): boolean {
    return this.permissionService.canAction('clientes', 'activate');
  }

  ngOnInit(): void {
    // Sincronizar paginación inicial
    this.filtros.pageNumber = this.paginacion.page;
    this.filtros.pageSize = this.paginacion.limit;
    this.loadClientes();
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }

  /**
   * Carga la lista de clientes
   */
  loadClientes(): void {
    this.loading = true;
    this.error = null;

    // Asegurar que los filtros tengan la paginación actual
    this.filters.pageNumber = this.paginacion.page;
    this.filters.pageSize = this.paginacion.limit;

    this.clienteService
      .getClientes(this.filters)
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: (response: any) => {
          this.clientes = response.data;

          // Actualizar objeto de paginación para el template
          this.paginacion.total = response.totalCount;
          // response.pageNumber might be returned or we assume current request page
          // backend PagedResponse has pageNumber, pageSize, totalCount
          this.paginacion.page = response.pageNumber;

          // Actualizar variables individuales (legacy/compatibilidad)
          this.totalClientes = response.totalCount;
          this.currentPage = response.pageNumber;
          this.totalPages = response.totalPages;

          this.loading = false;
        },
        error: error => {
          this.error = 'Error al cargar los clientes';
          this.loading = false;
          console.error('Error loading clientes:', error);
        },
      });
  }

  /**
   * Maneja el evento de búsqueda
   */
  onSearch(searchTerm: string): void {
    if (searchTerm.trim()) {
      this.filters.search = searchTerm;
    } else {
      this.filters.search = undefined;
    }
    this.currentPage = 1;
    this.loadClientes();
  }

  /**
   * Limpia los filtros de búsqueda
   */
  limpiarFiltros(): void {
    this.searchTerm = '';
    this.filters = {
      pageNumber: 1,
      pageSize: this.paginacion.limit,
    };
    this.paginacion.page = 1;
    this.loadClientes();
  }

  /**
   * Maneja el evento de tecla presionada en el campo de búsqueda
   */
  onSearchKeyPress(event: KeyboardEvent): void {
    if (event.key === 'Enter') {
      this.onSearch(this.searchTerm);
    }
  }

  /**
   * Maneja la búsqueda de clientes
   */
  onSearchEvent(event: Event): void {
    const target = event.target as HTMLInputElement;
    this.searchTerm = target.value;
    this.searchSubject.next(this.searchTerm);
  }

  toggleFilterMenu(): void {
    this.showFilterMenu = !this.showFilterMenu;
  }

  selectEstado(estado: string): void {
    this.selectedEstado = estado;
    this.showFilterMenu = false;
    this.paginacion.page = 1;
    this.loadClientes();
  }

  /**
   * Busca cliente por identificación
   */
  buscarClientePorId(): void {
    if (!this.searchTerm.trim()) {
      this.loadClientes();
      return;
    }

    this.loading = true;
    this.clienteService
      .buscarClientePorNombre(this.searchTerm.trim())
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: (cliente: Cliente | null) => {
          this.clientes = cliente ? [cliente] : [];
          this.totalClientes = cliente ? 1 : 0;
          this.loading = false;
        },
        error: (_error: any) => {
          this.error = 'Cliente no encontrado';
          this.clientes = [];
          this.totalClientes = 0;
          this.loading = false;
        },
      });
  }

  /**
   * Abre el modal para crear un nuevo cliente
   */
  crearCliente(): void {
    if (
      !this.permissionService.guardAction(
        'clientes',
        'create',
        'No tienes permisos para crear clientes.'
      )
    ) {
      return;
    }

    this.selectedCliente = null;
    this.modalMode = 'create';
    this.showModal = true;
  }

  /**
   * Abre el modal para editar un cliente
   */
  editarCliente(cliente: Cliente): void {
    if (
      !this.permissionService.guardAction(
        'clientes',
        'edit',
        'No tienes permisos para editar clientes.'
      )
    ) {
      return;
    }

    this.selectedCliente = { ...cliente }; // Crear copia para evitar mutaciones
    this.modalMode = 'edit';
    this.showModal = true;
  }

  /**
   * Abre el modal para ver los detalles de un cliente
   */
  verCliente(cliente: Cliente): void {
    this.selectedCliente = { ...cliente }; // Crear copia para evitar mutaciones
    this.modalMode = 'view';
    this.showModal = true;
  }

  /**
   * Abre el modal para crear cliente
   */
  openCreateModal(): void {
    if (
      !this.permissionService.guardAction(
        'clientes',
        'create',
        'No tienes permisos para crear clientes.'
      )
    ) {
      return;
    }

    this.modalState = {
      isOpen: true,
      action: 'create',
    };
  }

  /**
   * Abre el modal para editar cliente
   */
  openEditModal(cliente: Cliente): void {
    if (
      !this.permissionService.guardAction(
        'clientes',
        'edit',
        'No tienes permisos para editar clientes.'
      )
    ) {
      return;
    }

    this.modalState = {
      isOpen: true,
      action: 'edit',
      cliente: { ...cliente },
    };
  }

  /**
   * Abre el modal de confirmación para eliminar cliente
   */
  eliminarCliente(cliente: Cliente): void {
    if (
      !this.permissionService.guardAction(
        'clientes',
        'delete',
        'No tienes permisos para eliminar clientes.'
      )
    ) {
      return;
    }

    console.log('Solicitud de eliminación para:', cliente);
    this.clienteAEliminar = cliente;
    this.showDeleteConfirm = true;
    this.error = null;
  }

  /**
   * Confirma la eliminación del cliente
   */
  confirmarEliminacion(): void {
    if (
      !this.permissionService.guardAction(
        'clientes',
        'delete',
        'No tienes permisos para eliminar clientes.'
      )
    ) {
      return;
    }

    if (!this.clienteAEliminar) {
      console.error('No hay cliente seleccionado para eliminar');
      return;
    }

    console.log(
      'Confirmando eliminación de:',
      this.clienteAEliminar.cliente_id
    );
    this.loading = true;
    this.clienteService
      .deleteCliente(this.clienteAEliminar.cliente_id)
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: () => {
          console.log('Cliente eliminado con éxito');
          this.loading = false;
          this.showDeleteConfirm = false;
          this.showDeleteSuccess = true;
          this.clienteAEliminar = null;
          this.loadClientes(); // Recargar la lista
        },
        error: _error => {
          console.error('Error al eliminar cliente:', _error);
          this.loading = false;
          this.error =
            'Error al eliminar el cliente. Por favor, intente nuevamente.';
          // No cerramos el modal para que el usuario vea el error,
          // o podemos cerrarlo y mostrar un toast/alerta.
          // Por consistencia con el diseño actual, mantenemos el modal abierto o mostramos error.
          // En este caso, cerraremos el modal y dejaremos que el error se muestre en la lista si es necesario,
          // pero idealmente deberíamos mostrarlo en el modal.
          this.showDeleteConfirm = false;
        },
      });
  }

  /**
   * Cancela la eliminación del cliente
   */
  cancelarEliminacion(): void {
    this.showDeleteConfirm = false;
    this.clienteAEliminar = null;
  }

  /**
   * Cierra el modal de éxito de eliminación
   */
  cerrarModalEliminacion(): void {
    this.showDeleteSuccess = false;
    this.clienteAEliminar = null;
    this.loadClientes();
  }

  /**
   * Abre el modal de confirmación para dar de baja cliente
   */
  darDeBajaCliente(cliente: Cliente): void {
    if (
      !this.permissionService.guardAction(
        'clientes',
        'deactivate',
        'No tienes permisos para desactivar clientes.'
      )
    ) {
      return;
    }

    this.clienteADarDeBaja = cliente;
    this.showDeactivateConfirm = true;
  }

  /**
   * Confirma dar de baja al cliente
   */
  confirmarDarDeBaja(): void {
    if (
      !this.permissionService.guardAction(
        'clientes',
        'deactivate',
        'No tienes permisos para desactivar clientes.'
      )
    ) {
      return;
    }

    if (!this.clienteADarDeBaja) return;

    this.loading = true;
    this.clienteService
      .deactivateCliente(this.clienteADarDeBaja.cliente_id)
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: () => {
          this.loading = false;
          this.showDeactivateConfirm = false;
          this.showDeactivateSuccess = true;
          this.loadClientes(); // Recargar la lista
        },
        error: _error => {
          console.error('Error al dar de baja cliente:', _error);
          this.loading = false;
          this.error =
            'Error al dar de baja el cliente. Por favor, intente nuevamente.';
          this.showDeactivateConfirm = false;
        },
      });
  }

  /**
   * Cancela dar de baja al cliente
   */
  cancelarDarDeBaja(): void {
    this.showDeactivateConfirm = false;
    this.clienteADarDeBaja = null;
  }

  /**
   * Cierra el modal de éxito de dar de baja
   */
  cerrarModalDarDeBaja(): void {
    this.showDeactivateSuccess = false;
    this.clienteADarDeBaja = null;
    this.loadClientes();
  }

  /**
   * Abre el modal de confirmación para activar cliente
   */
  activarCliente(cliente: Cliente): void {
    if (
      !this.permissionService.guardAction(
        'clientes',
        'activate',
        'No tienes permisos para activar clientes.'
      )
    ) {
      return;
    }

    this.clienteAActivar = cliente;
    this.showActivateConfirm = true;
  }

  /**
   * Confirma activar al cliente
   */
  confirmarActivacion(): void {
    if (
      !this.permissionService.guardAction(
        'clientes',
        'activate',
        'No tienes permisos para activar clientes.'
      )
    ) {
      return;
    }

    if (!this.clienteAActivar) return;

    this.loading = true;
    this.clienteService
      .activateCliente(this.clienteAActivar.cliente_id)
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: () => {
          this.loading = false;
          this.showActivateConfirm = false;
          this.showActivateSuccess = true;
          this.loadClientes();
        },
        error: _error => {
          console.error('Error al activar cliente:', _error);
          this.loading = false;
          this.error = 'Error al activar el cliente. Por favor, intente nuevamente.';
          this.showActivateConfirm = false;
        },
      });
  }

  /**
   * Cancela activar al cliente
   */
  cancelarActivacion(): void {
    this.showActivateConfirm = false;
    this.clienteAActivar = null;
  }

  /**
   * Cierra el modal de éxito de activación
   */
  cerrarModalActivacion(): void {
    this.showActivateSuccess = false;
    this.clienteAActivar = null;
    this.loadClientes();
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

  /**
   * Abre el modal para eliminar cliente
   */
  openDeleteModal(cliente: Cliente): void {
    if (
      !this.permissionService.guardAction(
        'clientes',
        'delete',
        'No tienes permisos para eliminar clientes.'
      )
    ) {
      return;
    }

    this.modalState = {
      isOpen: true,
      action: 'delete',
      cliente,
    };
  }

  /**
   * Abre el modal para desactivar cliente
   */
  openDeactivateModal(cliente: Cliente): void {
    if (
      !this.permissionService.guardAction(
        'clientes',
        'deactivate',
        'No tienes permisos para desactivar clientes.'
      )
    ) {
      return;
    }

    this.modalState = {
      isOpen: true,
      action: 'deactivate',
      cliente,
    };
  }

  /**
   * Cierra el modal
   */
  closeModal(): void {
    this.modalState = {
      isOpen: false,
      action: 'create',
    };
  }

  /**
   * Maneja el evento de cliente guardado desde el modal
   */
  onClienteGuardado(_cliente: Cliente): void {
    // El modal de éxito ya se muestra automáticamente en el componente cliente-modal
    // Solo necesitamos recargar la lista cuando se cierre el modal de éxito
    this.loadClientes();
  }

  /**
   * Maneja el evento de cierre del modal
   */
  onModalCerrado(): void {
    this.showModal = false;
    this.selectedCliente = null;
    this.modalMode = 'create';
  }

  /**
   * Obtiene el texto para mostrar cuando no hay datos
   */
  getNoDataMessage(): string {
    if (this.loading) {
      return 'Cargando clientes...';
    }
    if (this.filtros.search) {
      return `No se encontraron clientes que coincidan con "${this.filtros.search}"`;
    }
    return 'No hay clientes registrados';
  }

  /**
   * Maneja el éxito de operaciones del modal
   */
  onModalSuccess(): void {
    this.closeModal();
    this.loadClientes();
  }

  /**
   * Cambia de página
   */
  changePage(page: number): void {
    if (page >= 1 && page <= this.totalPages) {
      this.paginacion.page = page;
      this.filters.pageNumber = page;
      this.loadClientes();
    }
  }

  nextPage(): void {
    if (this.paginacion.page < this.totalPages) {
      this.changePage(this.paginacion.page + 1);
    }
  }

  previousPage(): void {
    if (this.paginacion.page > 1) {
      this.changePage(this.paginacion.page - 1);
    }
  }

  /**
   * Obtiene las páginas para la paginación
   */
  getPages(): number[] {
    const pages: number[] = [];
    for (let i = 1; i <= this.totalPages; i++) {
      pages.push(i);
    }
    return pages;
  }

  getPageNumbers(): number[] {
    return this.getPages();
  }

  /**
   * TrackBy function para optimizar el renderizado de la lista
   */
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

  trackByClienteId(index: number, cliente: Cliente): number {
    return cliente.cliente_id;
  }
}
