import { Routes } from '@angular/router';
import { AuthGuard } from '@core/guards/auth.guard';
import { VIEW_ACCESS } from '@core/auth/view-access.config';

export const routes: Routes = [
  {
    path: '',
    redirectTo: '/login',
    pathMatch: 'full',
  },
  {
    path: 'login',
    loadComponent: () =>
      import('@features/auth/components/login/login.component').then(
        m => m.LoginComponent
      ),
  },
  {
    path: 'menu',
    canActivate: [AuthGuard],
    loadChildren: () =>
      import('@features/menu/menu.routes').then(m => m.menuRoutes),
  },
  {
    path: 'clientes',
    canActivate: [AuthGuard],
    data: { allowedRoles: VIEW_ACCESS.clientes },
    loadChildren: () =>
      import('@features/clientes/clientes.routes').then(m => m.clientesRoutes),
  },
  {
    path: 'menu-reportes',
    canActivate: [AuthGuard],
    data: { allowedRoles: VIEW_ACCESS['menu-reportes'] },
    loadComponent: () =>
      import('@features/menu-reporte/menu-reporte.component').then(
        m => m.MenuReporteComponent
      ),
  },
  {
    path: 'reportes',
    canActivate: [AuthGuard],
    data: { allowedRoles: VIEW_ACCESS.reportes },
    loadChildren: () =>
      import('@features/reportes/reportes.routes').then(m => m.reportesRoutes),
  },
  {
    path: 'proveedores',
    canActivate: [AuthGuard],
    data: { allowedRoles: VIEW_ACCESS.proveedores },
    loadComponent: () =>
      import('@features/proveedores/components/proveedoresView/proveedores.component').then(
        m => m.ProveedoresComponent
      ),
  },
  {
    path: 'inventarios',
    canActivate: [AuthGuard],
    data: { allowedRoles: VIEW_ACCESS.inventarios },
    loadComponent: () =>
      import('@features/inventarios/components/inventario-list/inventario-list.component').then(
        m => m.InventarioListComponent
      ),
  },
  {
    path: 'productos',
    canActivate: [AuthGuard],
    data: { allowedRoles: VIEW_ACCESS.productos },
    loadComponent: () =>
      import('./features/productos/components/productos-view/productos-view.component').then(
        m => m.ProductosViewComponent
      ),
  },
  {
    path: 'usuarios',
    canActivate: [AuthGuard],
    data: { allowedRoles: VIEW_ACCESS.usuarios },
    loadComponent: () =>
      import('./features/usuarios/components/usuarios-view/usuarios-view.component').then(
        m => m.UsuariosViewComponent
      ),
  },
  {
    path: 'tareas',
    canActivate: [AuthGuard],
    data: { allowedRoles: VIEW_ACCESS.tareas },
    loadComponent: () =>
      import('./features/tareas/components/tareas-view/tareas-view.component').then(
        m => m.TareasViewComponent
      ),
  },
  {
    path: 'subcategorias',
    canActivate: [AuthGuard],
    data: { allowedRoles: VIEW_ACCESS.subcategorias },
    loadComponent: () =>
      import('./features/subcategorias/components/subcategorias-view/subcategorias-view.component').then(
        m => m.SubcategoriasViewComponent
      ),
  },
  {
    path: 'provisiones',
    canActivate: [AuthGuard],
    data: { allowedRoles: VIEW_ACCESS.provisiones },
    loadComponent: () =>
      import('./features/provisiones/components/provisiones-view/provisiones-view.component').then(
        m => m.ProvisionesViewComponent
      ),
  },
  {
    path: 'categorias',
    canActivate: [AuthGuard],
    data: { allowedRoles: VIEW_ACCESS.categorias },
    loadComponent: () =>
      import('@features/categorias/components/categorias-view/categorias-view.component').then(
        m => m.CategoriasViewComponent
      ),
  },
  {
    path: 'dashboard',
    canActivate: [AuthGuard],
    loadComponent: () =>
      import('@features/menu/components/menu-dashboard/menu-dashboard.component').then(
        m => m.MenuDashboardComponent
      ),
  },
  {
    path: '**',
    redirectTo: '/menu',
  },
];
