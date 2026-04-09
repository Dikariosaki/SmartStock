import { Injectable } from '@angular/core';
import {
  ActivatedRouteSnapshot,
  CanActivate,
  Router,
  RouterStateSnapshot,
  UrlTree,
} from '@angular/router';
import { Observable } from 'rxjs';
import { AppRole, hasAnyRole } from '@core/auth/view-access.config';
import { AuthService } from '@core/services/auth.service';

@Injectable({
  providedIn: 'root',
})
export class AuthGuard implements CanActivate {
  constructor(
    private authService: AuthService,
    private router: Router
  ) {}

  canActivate(
    route: ActivatedRouteSnapshot,
    state: RouterStateSnapshot
  ):
    | Observable<boolean | UrlTree>
    | Promise<boolean | UrlTree>
    | boolean
    | UrlTree {
    if (this.authService.isAuthenticated()) {
      const allowedRoles = route.data['allowedRoles'] as
        | readonly AppRole[]
        | undefined;

      if (
        allowedRoles?.length &&
        !hasAnyRole(this.authService.currentUserValue?.role, allowedRoles)
      ) {
        return this.router.createUrlTree(['/menu']);
      }

      return true;
    }

    return this.router.createUrlTree(['/login'], {
      queryParams: { returnUrl: state.url },
    });
  }
}
