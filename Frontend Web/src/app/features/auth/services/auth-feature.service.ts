import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { HttpService } from '@core/services/http.service';
import {
  LoginCredentials,
  LoginResponse,
  RegisterRequest,
  ChangePasswordRequest,
  User,
} from '@features/auth/models/auth.models';

@Injectable({
  providedIn: 'root',
})
export class AuthFeatureService {
  constructor(private httpService: HttpService) {}

  login(credentials: LoginCredentials): Observable<any> {
    return this.httpService.post<LoginResponse>('Auth/login', credentials);
  }

  register(userData: RegisterRequest): Observable<any> {
    return this.httpService.post<User>('auth/register', userData);
  }

  refreshToken(refreshToken: string): Observable<any> {
    return this.httpService.post<LoginResponse>('auth/refresh', {
      refreshToken,
    });
  }

  changePassword(passwordData: ChangePasswordRequest): Observable<any> {
    return this.httpService.put<boolean>('auth/change-password', passwordData);
  }

  forgotPassword(email: string): Observable<any> {
    return this.httpService.post<boolean>('auth/forgot-password', { email });
  }

  resetPassword(token: string, newPassword: string): Observable<any> {
    return this.httpService.post<boolean>('auth/reset-password', {
      token,
      newPassword,
    });
  }

  verifyEmail(token: string): Observable<any> {
    return this.httpService.post<boolean>('auth/verify-email', { token });
  }

  getUserProfile(): Observable<any> {
    return this.httpService.get<User>('auth/profile');
  }

  updateProfile(userData: Partial<User>): Observable<any> {
    return this.httpService.put<User>('auth/profile', userData);
  }
}
