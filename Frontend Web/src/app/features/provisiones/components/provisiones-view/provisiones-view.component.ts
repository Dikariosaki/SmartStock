import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { SidebarComponent } from '@shared/ui/layout/sidebar/sidebar.component';
import { ReordenModalComponent } from '@features/inventarios/components/reorden-modal/reorden-modal.component';
import { BellNotificationComponent } from '@shared/ui/notifications/bell-notification.component';
import { Inventario } from '@features/inventarios/models/inventario.models';
import {
  ProvisionesService,
  OrdenProvision,
  OrdenProvisionItem,
} from '@features/provisiones/services/provisiones.service';
import { PermissionService } from '@core/auth/permission.service';
import { NotificationService } from '@shared/services/notification.service';

@Component({
  selector: 'app-provisiones-view',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    SidebarComponent,
    ReordenModalComponent,
    BellNotificationComponent,
  ],
  templateUrl: './provisiones-view.component.html',
  styleUrls: ['./provisiones-view.component.css'],
})
export class ProvisionesViewComponent implements OnInit {
  ordenes: OrdenProvision[] = [];
  ordenMap = new Map<number, OrdenProvision>();
  items: OrdenProvisionItem[] = [];
  filtered: OrdenProvisionItem[] = [];
  pageItems: OrdenProvisionItem[] = [];
  loading = false;
  error: string | null = null;
  search = '';
  selectedEstado: string = 'all';
  showFilterMenu = false;
  showReorden = false;
  inventarioParaReorden: Inventario | null = null;
  paginacion = { page: 1, limit: 30, total: 0 };
  Math = Math;

  constructor(
    private provisionesService: ProvisionesService,
    private permissionService: PermissionService,
    private notificationService: NotificationService
  ) {}

  get canCompleteOrden(): boolean {
    return this.permissionService.canAction('provisiones', 'completeOrden');
  }

  get canCompleteItem(): boolean {
    return this.permissionService.canAction('provisiones', 'completeItem');
  }

  get canManageReorden(): boolean {
    return this.permissionService.canAction('provisiones', 'manageReorden');
  }

  ngOnInit(): void {
    this.load();
  }

  toggleFilterMenu(): void {
    this.showFilterMenu = !this.showFilterMenu;
  }

  selectEstado(estado: string): void {
    this.selectedEstado = estado;
    this.showFilterMenu = false;
    this.paginacion.page = 1;
    this.load();
  }

  load(): void {
    this.loading = true;
    this.error = null;
    this.provisionesService.getOrdenes(this.paginacion.page, this.paginacion.limit).subscribe({
      next: (resOrdenes) => {
        this.ordenes = resOrdenes.data;
        // Note: Total is total orders. Pagination is driven by orders.
        this.paginacion.total = resOrdenes.totalCount; 
        this.ordenMap = new Map<number, OrdenProvision>(
          this.ordenes.map(o => [o.ordenId, o])
        );
        this.provisionesService.getItems(this.paginacion.page, this.paginacion.limit).subscribe({
          next: (resItems) => {
            this.items = resItems.data;
            this.applyFilter();
            this.loading = false;
          },
          error: () => {
            this.error = 'Error al cargar ítems de provisión';
            this.loading = false;
          },
        });
      },
      error: () => {
        this.error = 'Error al cargar órdenes de provisión';
        this.loading = false;
      },
    });
  }

  applyFilter(): void {
    const term = this.search.trim().toLowerCase();
    this.filtered = this.items.filter(
      it => {
        const matchesTerm = !term ||
          it.proveedorNombre.toLowerCase().includes(term) ||
          it.productoNombre.toLowerCase().includes(term) ||
          (it.estado ? 'completada' : 'pendiente').includes(term) ||
          (it.ordenEstado || '').toLowerCase().includes(term);
        
        let matchesEstado = true;
        if (this.selectedEstado === 'active') {
          matchesEstado = !it.estado; // Active = Pendiente (false)
        } else if (this.selectedEstado === 'inactive') {
          matchesEstado = it.estado; // Inactive = Completada (true)
        }

        return matchesTerm && matchesEstado;
      }
    );
    this.updatePage();
  }

  completarItem(it: OrdenProvisionItem): void {
    if (
      !this.permissionService.guardAction(
        'provisiones',
        'completeItem',
        'No tienes permisos para completar items de provisiones.'
      )
    ) {
      return;
    }

    if (it.estado) return;
    this.loading = true;
    this.provisionesService.completarItem(it.ordenId, it.productoId).subscribe({
      next: () => {
        this.notificationService.refreshInventoryAlerts();
        this.load();
      },
      error: () => {
        this.error = 'No se pudo completar el ítem';
        this.loading = false;
      },
    });
  }

  completarOrdenPorId(ordenId: number): void {
    if (
      !this.permissionService.guardAction(
        'provisiones',
        'completeOrden',
        'No tienes permisos para completar ordenes de provisiones.'
      )
    ) {
      return;
    }

    const orden = this.ordenMap.get(ordenId);
    if (!orden || orden.estado.toLowerCase() !== 'pendiente') return;
    this.loading = true;
    this.provisionesService.completarOrden(orden).subscribe({
      next: () => {
        this.notificationService.refreshInventoryAlerts();
        this.load();
      },
      error: () => {
        this.error = 'No se pudo completar la orden';
        this.loading = false;
      },
    });
  }

  trackByOrdenId(_index: number, it: OrdenProvisionItem): number {
    return it.ordenId * 100000 + it.productoId;
  }

  abrirReorden(it: OrdenProvisionItem): void {
    if (
      !this.permissionService.guardAction(
        'provisiones',
        'manageReorden',
        'No tienes permisos para gestionar reordenes de provisiones.'
      )
    ) {
      return;
    }

    if ((it.ordenEstado || '').toLowerCase() !== 'pendiente') return;
    this.inventarioParaReorden = {
      inventarioId: 0,
      productoId: it.productoId,
      ubicacion: '',
      cantidad: 0,
      puntoReorden: 0,
      estado: true,
    };
    this.showReorden = true;
  }

  onReordenClosed(): void {
    this.showReorden = false;
    this.inventarioParaReorden = null;
  }

  onReordenSuccess(_event: any): void {
    this.showReorden = false;
    this.inventarioParaReorden = null;
    this.notificationService.refreshInventoryAlerts();
  }

  get totalPaginas(): number {
    return Math.max(
      Math.ceil(this.paginacion.total / this.paginacion.limit),
      1
    );
  }

  updatePage(): void {
    // Nota: La paginación es controlada por el backend (por órdenes).
    // Aquí solo mostramos los items filtrados de la página actual.
    this.pageItems = this.filtered;
  }

  previousPage(): void {
    if (this.paginacion.page > 1) {
      this.paginacion.page -= 1;
      this.updatePage();
    }
  }

  nextPage(): void {
    if (this.paginacion.page < this.totalPaginas) {
      this.paginacion.page += 1;
      this.updatePage();
    }
  }

  selectPage(page: number): void {
    if (page < 1 || page > this.totalPaginas || page === this.paginacion.page)
      return;
    this.paginacion.page = page;
    this.updatePage();
  }

  getPageNumbers(): number[] {
    const total = this.totalPaginas;
    return Array.from({ length: total }, (_, i) => i + 1);
  }

  toggleDarkMode(): void {
    document.documentElement.classList.toggle('dark');
  }
}
