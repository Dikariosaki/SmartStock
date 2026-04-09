import { Component, Input, OnInit, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Inventario } from '@features/inventarios/models/inventario.models';
import { NotificationService } from '@shared/services/notification.service';
import { Subscription } from 'rxjs';
import { ReordenModalComponent } from '@features/inventarios/components/reorden-modal/reorden-modal.component';
import { PermissionService } from '@core/auth/permission.service';

@Component({
  selector: 'app-bell-notification',
  standalone: true,
  imports: [CommonModule, ReordenModalComponent],
  templateUrl: './bell-notification.component.html',
  styleUrls: ['./bell-notification.component.css'],
})
export class BellNotificationComponent implements OnInit, OnDestroy {
  @Input() feature: 'inventarios' | 'productos' | 'tareas' | 'provisiones' =
    'inventarios';
  count = 0;
  loading = false;
  open = false;
  items: Inventario[] = [];
  private sub?: Subscription;
  showReordenModal = false;
  inventarioReorden: Inventario | null = null;

  constructor(
    private notifications: NotificationService,
    private permissionService: PermissionService
  ) {}

  get canManageReorden(): boolean {
    return this.permissionService.canAction('provisiones', 'manageReorden');
  }

  ngOnInit(): void {
    this.notifications.startInventoryPolling(60000);
    this.sub = this.notifications.inventoryAlerts$.subscribe(list => {
      this.items = list;
      this.count = list.length;
    });
    this.refresh();
  }

  ngOnDestroy(): void {
    this.sub?.unsubscribe();
    this.notifications.stopInventoryPolling();
  }

  toggle(): void {
    this.open = !this.open;
  }

  refresh(): void {
    this.notifications.refreshInventoryAlerts();
  }

  openReorder(item: Inventario): void {
    if (
      !this.permissionService.guardAction(
        'provisiones',
        'manageReorden',
        'No tienes permisos para gestionar reordenes.'
      )
    ) {
      return;
    }

    this.inventarioReorden = item;
    this.showReordenModal = true;
    this.open = false;
  }

  onReordenSuccess(_payload: {
    proveedor: any;
    cantidad: number;
    precioUnitario: number;
    total: number;
  }): void {
    this.showReordenModal = false;
    this.inventarioReorden = null;
    this.refresh();
  }

  closeReordenModal(): void {
    this.showReordenModal = false;
    this.inventarioReorden = null;
  }

  onOrderClick(event: Event, item: Inventario): void {
    event.stopPropagation();
    this.openReorder(item);
  }
}
