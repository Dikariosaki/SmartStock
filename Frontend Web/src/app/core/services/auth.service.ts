import { Injectable } from '@angular/core';
import { BehaviorSubject, Observable } from 'rxjs';
import { Router } from '@angular/router';
import { jwtVerify } from 'jose';
import { HttpService } from './http.service';
import { config } from '@environments/environment';
import { getRoleFromId, normalizeRole } from '@core/auth/view-access.config';

export interface User {
  id: number;
  username: string;
  email: string;
  role: string;
  nombre?: string;
  cedula?: number;
  telefono?: string;
}

export interface LoginCredentials {
  email: string;
  password: string;
}

export interface AuthResponse {
  success: boolean;
  token?: string;
  user?: User;
  message?: string;
}

// Response from backend API
interface BackendAuthResponse {
  token: string;
  usuarioId: number;
  rolId: number;
  nombre: string;
  email: string;
}

@Injectable({
  providedIn: 'root',
})
export class AuthService {
  private currentUserSubject = new BehaviorSubject<User | null>(null);
  public currentUser$ = this.currentUserSubject.asObservable();
  private readonly TOKEN_KEY = 'auth_token';
  private readonly USER_KEY = 'current_user';
  private readonly JWT_SECRET = config.security.jwtSecret;

  constructor(
    private router: Router,
    private httpService: HttpService
  ) {
    // Check if user is stored in localStorage and token is valid
    this.initializeAuth();
  }

  private initializeAuth(): void {
    const token = this.getToken();
    const storedUser = localStorage.getItem(this.USER_KEY);

    if (token && storedUser && this.isTokenValid(token)) {
      const parsedUser = JSON.parse(storedUser) as User;
      const tokenUser = this.getUserFromRawToken(token);
      const hydratedUser: User = {
        ...parsedUser,
        ...tokenUser,
        role:
          tokenUser?.role ||
          normalizeRole(parsedUser.role) ||
          parsedUser.role,
      };

      localStorage.setItem(this.USER_KEY, JSON.stringify(hydratedUser));
      this.currentUserSubject.next(hydratedUser);
    } else {
      // Clear invalid data
      this.clearAuthData();
    }
  }

  login(credentials: LoginCredentials): Observable<AuthResponse> {
    return new Observable(observer => {
      // Call backend API for authentication
      this.httpService
        .post<BackendAuthResponse>(`${config.api.endpoints.auth}/login`, {
          email: credentials.email,
          password: credentials.password,
        })
        .subscribe({
          next: response => {
            const tokenUser = this.getUserFromRawToken(response.token);

            // Convert backend response to User format
            const user: User = {
              id: response.usuarioId,
              username: response.nombre,
              email: response.email,
              role: tokenUser?.role || this.mapRoleId(response.rolId),
              nombre: response.nombre,
              cedula: tokenUser?.cedula,
              telefono: tokenUser?.telefono,
            };

            // Store token and user data
            localStorage.setItem(this.TOKEN_KEY, response.token);
            localStorage.setItem(this.USER_KEY, JSON.stringify(user));
            this.currentUserSubject.next(user);

            observer.next({
              success: true,
              token: response.token,
              user,
              message: 'Login exitoso',
            });
            observer.complete();
          },
          error: (error: any) => {
            console.error('Error en autenticación:', error);
            const message =
              error.error?.message || error.message || 'Credenciales inválidas';
            observer.next({
              success: false,
              message,
            });
            observer.complete();
          },
        });
    });
  }

  logout(): void {
    this.clearAuthData();
    this.router.navigate(['/login']);
  }

  private clearAuthData(): void {
    localStorage.removeItem(this.TOKEN_KEY);
    localStorage.removeItem(this.USER_KEY);
    this.currentUserSubject.next(null);
  }

  getToken(): string | null {
    return localStorage.getItem(this.TOKEN_KEY);
  }

  get currentUserValue(): User | null {
    return this.currentUserSubject.value;
  }

  isAuthenticated(): boolean {
    const token = this.getToken();
    return token !== null && this.isTokenValid(token);
  }

  /**
   * Mapea el rol_id a un string de rol
   */
  private mapRoleId(rolId: number): string {
    return getRoleFromId(rolId);
  }

  private async isTokenValidAsync(token: string): Promise<boolean> {
    try {
      // Si this.JWT_SECRET ya es Uint8Array, no necesitamos codificarlo de nuevo
      const secret =
        this.JWT_SECRET instanceof Uint8Array
          ? this.JWT_SECRET
          : new TextEncoder().encode(this.JWT_SECRET as string);

      const { payload } = await jwtVerify(token, secret);
      const currentTime = Math.floor(Date.now() / 1000);
      return (payload.exp as number) > currentTime;
    } catch {
      return false;
    }
  }

  private isTokenValid(token: string): boolean {
    const payload = this.decodeTokenPayload(token);
    if (!payload) {
      return false;
    }

    return payload.exp > Math.floor(Date.now() / 1000);
  }

  private decodeTokenPayload(token: string): any | null {
    try {
      const parts = token.split('.');
      if (parts.length !== 3) return null;

      const base64Url = parts[1];
      const base64 = base64Url.replace(/-/g, '+').replace(/_/g, '/');
      const jsonPayload = decodeURIComponent(
        // eslint-disable-next-line no-undef
        atob(base64)
          .split('')
          .map(c => '%' + ('00' + c.charCodeAt(0).toString(16)).slice(-2))
          .join('')
      );
      return JSON.parse(jsonPayload);
    } catch {
      return null;
    }
  }

  getUserFromToken(): User | null {
    const token = this.getToken();
    if (!token || !this.isTokenValid(token)) return null;

    return this.getUserFromRawToken(token);
  }

  private getUserFromRawToken(token: string): User | null {
    const payload = this.decodeTokenPayload(token);
    if (!payload) {
      return null;
    }

    try {
      // Buscar el rol en diferentes posibles claims
      const role =
        normalizeRole(payload.role) ||
        normalizeRole(
          payload['http://schemas.microsoft.com/ws/2008/06/identity/claims/role']
        ) ||
        normalizeRole(payload.rol) ||
        (payload.rolId ? this.mapRoleId(Number(payload.rolId)) : undefined);

      if (payload.nombre && payload.cedula) {
        return {
          id: Number(payload.sub),
          username: payload.nombre || payload.email.split('@')[0],
          email: payload.email,
          role: role || 'auxiliar',
          nombre: payload.nombre,
          cedula: payload.cedula,
          telefono: payload.telefono,
        };
      } else {
        return {
          id: Number(payload.sub),
          username: payload.email.split('@')[0],
          email: payload.email,
          role: role || 'auxiliar',
        };
      }
    } catch {
      return null;
    }
  }

  /**
   * Actualiza los datos del usuario actual en el estado y localStorage
   */
  updateCurrentUser(updates: Partial<User>): void {
    const current = this.currentUserSubject.value;
    if (!current) return;

    // 1. Limpiar el objeto 'updates' de campos que NUNCA deben actualizarse desde un Partial (como el rol o id)
    // El rol solo debe cambiar si se especifica explícitamente y con validación (no desde el perfil)
    const sanitizedUpdates = { ...updates };
    
    // Si la actualización viene del perfil (no tiene rolId o password), protegemos el rol actual
    if (!sanitizedUpdates.role) {
      delete sanitizedUpdates.role;
    }

    // 2. Determinar el rol de forma ultra-segura
    const tokenUser = this.getUserFromToken();
    const finalRole =
      normalizeRole(sanitizedUpdates.role) ||
      normalizeRole(current.role) ||
      normalizeRole(tokenUser?.role) ||
      'auxiliar';

    // 3. Preparar el nuevo objeto usuario
    const updatedUser: User = {
      ...current,
      ...sanitizedUpdates,
      role: finalRole 
    };

    // 3. Mantener coherencia entre nombre y username
    if (updates.nombre && !updates.username) {
      updatedUser.username = updates.nombre;
    }

    // 4. Limpieza de valores basura que podrían haber corrompido el estado
    if (updatedUser.role === 'undefined' || updatedUser.role === 'null' || !updatedUser.role) {
      updatedUser.role = tokenUser?.role || 'auxiliar';
    }

    console.log(`[AuthService] Perfil actualizado. ID: ${updatedUser.id}, Rol: ${updatedUser.role}`);
    
    localStorage.setItem(this.USER_KEY, JSON.stringify(updatedUser));
    this.currentUserSubject.next(updatedUser);
  }
}
