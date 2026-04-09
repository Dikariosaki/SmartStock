import { Injectable } from '@angular/core';
import { Router } from '@angular/router';
import { canAccessRoute } from '@core/auth/view-access.config';
import { AuthService } from '@core/services/auth.service';

export interface SidebarItem {
  id: string;
  title: string;
  icon: string;
  route: string;
  isActive: boolean;
}

@Injectable({
  providedIn: 'root',
})
export class SidebarService {
  private sidebarItems: SidebarItem[] = [
    {
      id: 'usuarios',
      title: 'Usuarios',
      icon: 'person',
      route: '/usuarios',
      isActive: true,
    },
    {
      id: 'clientes',
      title: 'Clientes',
      icon: 'people',
      route: '/clientes',
      isActive: true,
    },
    {
      id: 'productos',
      title: 'Productos',
      icon: 'inventory_2',
      route: '/productos',
      isActive: true,
    },
    {
      id: 'reportes',
      title: 'Reportes',
      icon: 'bar_chart',
      route: '/reportes',
      isActive: true,
    },
    {
      id: 'provisiones',
      title: 'Provisiones',
      icon: 'move_to_inbox',
      route: '/provisiones',
      isActive: true,
    },
    {
      id: 'categorias',
      title: 'Categorías',
      icon: 'category',
      route: '/categorias',
      isActive: true,
    },
    {
      id: 'subcategorias',
      title: 'SubCategorías',
      icon: 'segment',
      route: '/subcategorias',
      isActive: true,
    },
    {
      id: 'tareas',
      title: 'Tareas',
      icon: 'assignment',
      route: '/tareas',
      isActive: true,
    },
    {
      id: 'proveedores',
      title: 'Proveedores',
      icon: 'local_shipping',
      route: '/proveedores',
      isActive: true,
    },
    {
      id: 'inventarios',
      title: 'Inventarios',
      icon: 'inventory',
      route: '/inventarios',
      isActive: true,
    },
  ];

  constructor(
    private router: Router,
    private authService: AuthService
  ) {}

  getSidebarItems(): SidebarItem[] {
    const currentRole = this.authService.currentUserValue?.role;

    return this.sidebarItems.filter(
      item => item.isActive && canAccessRoute(currentRole, item.route)
    );
  }

  navigateTo(route: string): void {
    this.router.navigate([route]);
  }

  isCurrentRoute(route: string): boolean {
    return this.router.url === route;
  }

  getProfileRoute(): string {
    return '/menu';
  }

  getHomeRoute(): string {
    return '/menu';
  }
}
