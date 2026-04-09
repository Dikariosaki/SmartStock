import { Routes } from '@angular/router';
import { MenuDashboardComponent } from './components/menu-dashboard/menu-dashboard.component';

export const menuRoutes: Routes = [
  {
    path: '',
    component: MenuDashboardComponent,
    title: 'SmartStock - Menú Principal',
  },
  {
    path: 'dashboard',
    component: MenuDashboardComponent,
    title: 'SmartStock - Dashboard',
  },
];
