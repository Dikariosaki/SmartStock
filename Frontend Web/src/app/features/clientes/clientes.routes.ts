import { Routes } from '@angular/router';
import { ClienteListComponent } from './components/cliente-list/cliente-list.component';

export const clientesRoutes: Routes = [
  {
    path: '',
    component: ClienteListComponent,
    title: 'Gestión de Clientes - SmartStock',
  },
  {
    path: 'lista',
    component: ClienteListComponent,
    title: 'Lista de Clientes - SmartStock',
  },
  {
    path: '**',
    redirectTo: '',
  },
];
