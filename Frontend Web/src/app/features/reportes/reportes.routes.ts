import { Routes } from '@angular/router';
import { ReportesMovimientosComponent } from './components/reportes-movimientos/reportes-movimientos.component';

export const reportesRoutes: Routes = [
  {
    path: 'entradas',
    component: ReportesMovimientosComponent,
    data: { tipo: 'entrada' }, // We can pass data or use a resolver, but component input binding via router is cleaner in newer Angular versions or just reuse component with different input
  },
  {
    path: 'salidas',
    component: ReportesMovimientosComponent,
    data: { tipo: 'salida' },
  },
  {
    path: '',
    redirectTo: 'entradas',
    pathMatch: 'full',
  },
];
