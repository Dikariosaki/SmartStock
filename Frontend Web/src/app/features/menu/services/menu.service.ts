import { Injectable } from '@angular/core';
import { BehaviorSubject, Observable, map, of, switchMap } from 'rxjs';
import { AuthService } from '@core/services/auth.service';
import { normalizeRole } from '@core/auth/view-access.config';
import { MenuConfig, MenuItem, UserProfile } from '../models/menu.models';
import { UpdateUsuarioRequest } from '../../usuarios/models/usuario.models';
import { UsuarioService } from '../../usuarios/services/usuarios.service';
import { SidebarItem, SidebarService } from '@shared/ui/layout/sidebar/sidebar.service';

@Injectable({
  providedIn: 'root',
})
export class MenuService {
  private userProfileSubject = new BehaviorSubject<UserProfile>({
    id: 1,
    name: 'Andres Olaya',
    identification: '1000335245',
    email: 'admin@SmartStock.com',
    phone: '3148895518',
    role: 'administrador',
  });

  public userProfile$ = this.userProfileSubject.asObservable();

  constructor(
    private authService: AuthService,
    private usuarioService: UsuarioService,
    private sidebarService: SidebarService
  ) {
    this.authService.currentUser$.subscribe(user => {
      if (!user) {
        return;
      }

      const currentProfile = this.userProfileSubject.value;
      const basicProfile: UserProfile = {
        id: user.id,
        name: user.nombre || user.username,
        identification: user.cedula?.toString() || '',
        email: user.email,
        phone: user.telefono || '',
        role: normalizeRole(user.role) || currentProfile?.role || 'auxiliar',
      };
      this.userProfileSubject.next(basicProfile);

      this.usuarioService.getUsuarioById(user.id).subscribe({
        next: usuarioCompleto => {
          const fullProfile: UserProfile = {
            id: usuarioCompleto.usuario_id,
            name: usuarioCompleto.nombre,
            identification: usuarioCompleto.identificacion || '',
            email: usuarioCompleto.correo,
            phone: usuarioCompleto.telefono || '',
            role:
              normalizeRole(usuarioCompleto.rolNombre) || basicProfile.role,
          };
          this.userProfileSubject.next(fullProfile);
        },
        error: err => {
          console.warn(
            '[MenuService] No se pudieron obtener datos completos del usuario:',
            err
          );
        },
      });
    });
  }

  getMenuItems(): Observable<MenuItem[]> {
    const allowedSidebarItems = this.sidebarService.getSidebarItems();

    const menuItems = allowedSidebarItems
      .map(item => this.mapSidebarToDashboardItem(item))
      .sort((a, b) => a.order - b.order);

    return of(menuItems);
  }

  private mapSidebarToDashboardItem(sidebarItem: SidebarItem): MenuItem {
    const dashboardLabels: Record<
      string,
      Pick<MenuItem, 'title' | 'description' | 'order'>
    > = {
      usuarios: { title: 'Gestion de', description: 'Usuarios', order: 1 },
      clientes: { title: 'Gestion de', description: 'Clientes', order: 2 },
      productos: { title: 'Gestion de', description: 'Productos', order: 3 },
      reportes: { title: 'Gestion de', description: 'Reportes', order: 4 },
      provisiones: {
        title: 'Alerta de',
        description: 'Provisiones',
        order: 5,
      },
      categorias: { title: 'Gestion de', description: 'Categorias', order: 6 },
      subcategorias: {
        title: 'Gestion de',
        description: 'Sub-Categorias',
        order: 7,
      },
      tareas: { title: 'Gestion de', description: 'Tareas', order: 8 },
      inventarios: { title: 'Inventarios', description: '', order: 9 },
      proveedores: { title: 'Proveedores', description: '', order: 10 },
    };

    const labels = dashboardLabels[sidebarItem.id] ?? {
      title: sidebarItem.title,
      description: '',
      order: 99,
    };

    return {
      id: sidebarItem.id,
      title: labels.title,
      description: labels.description,
      icon: sidebarItem.icon,
      route: sidebarItem.route,
      isActive: true,
      order: labels.order,
    };
  }

  getUserProfile(): Observable<UserProfile> {
    return this.userProfile$;
  }

  updateUserProfile(profile: Partial<UserProfile>): Observable<UserProfile> {
    const currentProfile = this.userProfileSubject.value;
    if (!currentProfile || !currentProfile.id) {
      return of(currentProfile);
    }

    const { name, identification, email, phone } = profile;
    const safeProfile = { name, identification, email, phone };

    return this.usuarioService.getUsuarioById(currentProfile.id).pipe(
      switchMap(usuarioActual => {
        const updateReq: UpdateUsuarioRequest = {
          nombre: safeProfile.name || usuarioActual.nombre,
          email: safeProfile.email || usuarioActual.correo,
          telefono: safeProfile.phone || usuarioActual.telefono,
          cedula: safeProfile.identification
            ? parseInt(safeProfile.identification)
            : parseInt(usuarioActual.identificacion),
          rolId: usuarioActual.rolId,
          estado: usuarioActual.activo ?? true,
        };

        return this.usuarioService.updateUsuarioObservable(
          currentProfile.id!,
          updateReq
        );
      }),
      switchMap(() => this.usuarioService.getUsuarioById(currentProfile.id!)),
      map(updatedUsuario => {
        const finalRole =
          normalizeRole(updatedUsuario.rolNombre) ||
          normalizeRole(currentProfile.role) ||
          'auxiliar';

        const updatedProfile: UserProfile = {
          id: updatedUsuario.usuario_id,
          name: updatedUsuario.nombre,
          identification: updatedUsuario.identificacion,
          email: updatedUsuario.correo,
          phone: updatedUsuario.telefono,
          role: finalRole,
        };

        this.userProfileSubject.next(updatedProfile);

        this.authService.updateCurrentUser({
          nombre: updatedProfile.name,
          email: updatedProfile.email,
          telefono: updatedProfile.phone,
          cedula: updatedProfile.identification
            ? parseInt(updatedProfile.identification)
            : undefined,
          role: finalRole,
        });

        return updatedProfile;
      })
    );
  }

  getMenuConfig(): Observable<MenuConfig> {
    return new Observable(observer => {
      this.getMenuItems().subscribe(items => {
        const config: MenuConfig = {
          sections: [
            {
              title: 'MENU',
              items,
            },
          ],
          userProfile: this.userProfileSubject.value,
        };
        observer.next(config);
        observer.complete();
      });
    });
  }
}
