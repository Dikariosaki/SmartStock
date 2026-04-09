import { Injectable } from '@angular/core';
import { BehaviorSubject, Subscription, timer, switchMap, map } from 'rxjs';
import { InventarioService } from '@features/inventarios/services/inventarios.service';
import { Inventario } from '@features/inventarios/models/inventario.models';

@Injectable({ providedIn: 'root' })
export class NotificationService {
  private inventoryAlertsSubject = new BehaviorSubject<Inventario[]>([]);
  inventoryAlerts$ = this.inventoryAlertsSubject.asObservable();
  private pollingSub?: Subscription;

  constructor(private inventarioService: InventarioService) {}

  refreshInventoryAlerts(): void {
    this.inventarioService
      .getInventariosBajoMinimo(50)
      .pipe(map(alertas => alertas.filter(alerta => this.isLowStock(alerta))))
      .subscribe({
        next: alertas => this.inventoryAlertsSubject.next(alertas),
        error: () => this.inventoryAlertsSubject.next([]),
      });
  }

  startInventoryPolling(intervalMs = 60000): void {
    this.stopInventoryPolling();
    this.pollingSub = timer(0, intervalMs)
      .pipe(
        switchMap(() => this.inventarioService.getInventariosBajoMinimo(50)),
        map(alertas => alertas.filter(alerta => this.isLowStock(alerta)))
      )
      .subscribe({
        next: alertas => this.inventoryAlertsSubject.next(alertas),
        error: () => this.inventoryAlertsSubject.next([]),
      });
  }

  stopInventoryPolling(): void {
    this.pollingSub?.unsubscribe();
    this.pollingSub = undefined;
  }

  private isLowStock(alerta: Inventario): boolean {
    return alerta.estado && alerta.cantidad < alerta.puntoReorden;
  }
}
