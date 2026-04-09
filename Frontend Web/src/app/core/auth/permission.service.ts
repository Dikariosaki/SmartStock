import { Injectable } from '@angular/core';
import {
  ActionKey,
  ModuleKey,
  ViewKey,
  canAccessAction,
  canAccessView,
} from '@core/auth/view-access.config';
import { AuthService } from '@core/services/auth.service';
import { ToastService } from '@shared/services/toast.service';

@Injectable({
  providedIn: 'root',
})
export class PermissionService {
  constructor(
    private authService: AuthService,
    private toastService: ToastService
  ) {}

  canView(viewKey: ViewKey): boolean {
    return canAccessView(this.authService.currentUserValue?.role, viewKey);
  }

  canAction(moduleKey: ModuleKey, action: ActionKey): boolean {
    return canAccessAction(
      this.authService.currentUserValue?.role,
      moduleKey,
      action
    );
  }

  guardAction(
    moduleKey: ModuleKey,
    action: ActionKey,
    deniedMessage?: string
  ): boolean {
    if (this.canAction(moduleKey, action)) {
      return true;
    }

    this.toastService.warning(
      deniedMessage || 'No tienes permisos para realizar esta accion.'
    );
    return false;
  }
}

