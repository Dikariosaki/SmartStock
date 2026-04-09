import { Component, OnInit, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Subject, takeUntil, debounceTime, distinctUntilChanged } from 'rxjs';
import { SidebarComponent } from '@/app/shared/ui/layout/sidebar';
import { CategoriasModalComponent } from '../categorias-modal/categorias-modal.component';
import { Categoria, CategoriaFilters } from '../../models/categoria.models';
import { CategoriaService } from '../../services/categorias.service';
import { BellNotificationComponent } from '@shared/ui/notifications/bell-notification.component';
import { PermissionService } from '@core/auth/permission.service';

@Component({
  selector: 'app-categorias-view',
  standalone: true,
  imports: [
    SidebarComponent,
    CategoriasModalComponent,
    CommonModule,
    FormsModule,
    CategoriasModalComponent,
    SidebarComponent,
    BellNotificationComponent,
  ],
  templateUrl: './categorias-view.component.html',
  styleUrls: ['./categorias-view.component.css'],
})
export class CategoriasViewComponent implements OnInit, OnDestroy {
  private destroy$ = new Subject<void>();
  private searchSubject = new Subject<string>();

  categorias: Categoria[] = [];
  filtros: CategoriaFilters = {};
  paginacion = { page: 1, limit: 10, total: 0 };
  loading = false;
  error: string | null = null;
  searchTerm = '';
  
  selectedEstado: string = 'all';
  showFilterMenu: boolean = false;

  selectedCategoria: Categoria | null = null;
  showModal = false;
  modalMode: 'create' | 'edit' | 'view' = 'create';

  showDeleteConfirm = false;
  showDeleteSuccess = false;
  categoriaAEliminar: Categoria | null = null;

  Math = Math;

  constructor(
    private categoriaService: CategoriaService,
    private permissionService: PermissionService
  ) {}

  get canCreateCategoria(): boolean {
    return this.permissionService.canAction('categorias', 'create');
  }

  get canEditCategoria(): boolean {
    return this.permissionService.canAction('categorias', 'edit');
  }

  get canDeleteCategoria(): boolean {
    return this.permissionService.canAction('categorias', 'delete');
  }

  ngOnInit(): void {
    this.cargarCategorias();
    this.searchSubject
      .pipe(debounceTime(300), distinctUntilChanged(), takeUntil(this.destroy$))
      .subscribe(term => {
        this.filtros.search = term;
        this.cargarCategorias();
      });
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }

  cargarCategorias(): void {
    this.loading = true;

    if (this.selectedEstado === 'active') {
      this.filtros.estado = true;
    } else if (this.selectedEstado === 'inactive') {
      this.filtros.estado = false;
    } else {
      this.filtros.estado = undefined;
    }

    this.categoriaService
      .getCategorias(this.paginacion.page, this.paginacion.limit, this.filtros)
      .subscribe({
        next: (res: any) => {
          this.categorias = res.data;
          this.paginacion.total = res.totalCount;
          this.loading = false;
        },
        error: () => {
          this.loading = false;
          this.error = 'Error al cargar categorías.';
        },
      });
  }

  onSearch(): void {
    this.searchSubject.next(this.searchTerm);
  }
  onSearchKeyPress(event: KeyboardEvent): void {
    if (event.key === 'Enter') this.onSearch();
  }
  onSearchEvent(event: Event): void {
    this.searchTerm = (event.target as HTMLInputElement).value;
    this.onSearch();
  }

  toggleFilterMenu(): void {
    this.showFilterMenu = !this.showFilterMenu;
  }

  selectEstado(estado: string): void {
    this.selectedEstado = estado;
    this.showFilterMenu = false;
    this.paginacion.page = 1;
    this.cargarCategorias();
  }

  get totalPages(): number {
    return Math.ceil(this.paginacion.total / this.paginacion.limit) || 1;
  }
  previousPage(): void {
    if (this.paginacion.page > 1) {
      this.paginacion.page--;
      this.cargarCategorias();
    }
  }
  nextPage(): void {
    if (this.paginacion.page < this.totalPages) {
      this.paginacion.page++;
      this.cargarCategorias();
    }
  }
  getPageNumbers(): number[] {
    return Array.from({ length: this.totalPages }, (_, i) => i + 1);
  }
  changePage(page: number): void {
    this.paginacion.page = page;
    this.cargarCategorias();
  }
  getFillerRows(): number[] {
    return Array(Math.max(0, this.paginacion.limit - this.categorias.length))
      .fill(0)
      .map((_, i) => i);
  }
  getNoDataMessage(): string {
    return 'No se encontraron categorías';
  }

  crearCategoria(): void {
    if (
      !this.permissionService.guardAction(
        'categorias',
        'create',
        'No tienes permisos para crear categorias.'
      )
    ) {
      return;
    }

    this.selectedCategoria = null;
    this.modalMode = 'create';
    this.showModal = true;
  }
  editarCategoria(_item: Categoria): void {
    if (
      !this.permissionService.guardAction(
        'categorias',
        'edit',
        'No tienes permisos para editar categorias.'
      )
    ) {
      return;
    }

    this.selectedCategoria = _item;
    this.modalMode = 'edit';
    this.showModal = true;
  }
  verCategoria(_item: Categoria): void {
    this.selectedCategoria = _item;
    this.modalMode = 'view';
    this.showModal = true;
  }

  onModalSuccess(_item: Categoria): void {
    this.cargarCategorias();
  }
  onModalClosed(): void {
    this.showModal = false;
    this.selectedCategoria = null;
  }

  // Eliminar
  eliminarCategoria(item: Categoria): void {
    if (
      !this.permissionService.guardAction(
        'categorias',
        'delete',
        'No tienes permisos para eliminar categorias.'
      )
    ) {
      return;
    }

    this.categoriaAEliminar = item;
    this.showDeleteConfirm = true;
  }
  confirmarEliminacion(): void {
    if (
      !this.permissionService.guardAction(
        'categorias',
        'delete',
        'No tienes permisos para eliminar categorias.'
      )
    ) {
      return;
    }

    if (!this.categoriaAEliminar) return;
    this.loading = true;
    this.categoriaService
      .deleteCategoria(this.categoriaAEliminar.categoriaId)
      .subscribe({
        next: () => {
          this.loading = false;
          this.showDeleteConfirm = false;
          this.showDeleteSuccess = true;
          this.cargarCategorias();
        },
        error: () => {
          this.loading = false;
          this.showDeleteConfirm = false;
        },
      });
  }
  cancelarEliminacion(): void {
    this.showDeleteConfirm = false;
    this.categoriaAEliminar = null;
  }
  cerrarModalEliminacion(): void {
    this.showDeleteSuccess = false;
  }

  onBackdropClick(event: Event): void {
    if (event.target === event.currentTarget) {
      if (this.showDeleteConfirm) this.cancelarEliminacion();
      else if (this.showDeleteSuccess) this.cerrarModalEliminacion();
    }
  }
  toggleDarkMode(): void {
    document.documentElement.classList.toggle('dark');
  }

  // ══════════════════════════════════════════
  // UTILIDADES
  // ══════════════════════════════════════════
  trackById(index: number, item: Categoria): number {
    return item.categoriaId;
  }
}
