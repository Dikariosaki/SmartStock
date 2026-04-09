import { Component, Input, Output, EventEmitter, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { UserProfile } from '../../models/menu.models';
import { AuthService } from '@core/services/auth.service';

@Component({
  selector: 'app-profile-modal',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './profile-modal.component.html',
  styleUrl: './profile-modal.component.css',
})
export class ProfileModalComponent implements OnInit {
  @Input() userProfile: UserProfile | null = null;
  @Output() close = new EventEmitter<void>();
  @Output() profileUpdate = new EventEmitter<Partial<UserProfile>>();

  formData: Partial<UserProfile> = {};

  isSubmitting = false;
  errorMessage: string | null = null;

  constructor(private authService: AuthService) {}

  ngOnInit(): void {
    if (this.userProfile) {
      this.formData = { ...this.userProfile };
    }
  }

  onOverlayClick(event: Event): void {
    if (event.target === event.currentTarget) {
      this.onCancel();
    }
  }

  onCancel(): void {
    if (!this.isSubmitting) {
      this.close.emit();
    }
  }

  onSubmit(): void {
    if (this.formData && !this.isSubmitting) {
      this.errorMessage = null;
      this.isSubmitting = true;
      // Solo emitir los campos editables — nunca incluir role, id o password
      this.profileUpdate.emit({
        name: this.formData.name,
        identification: this.formData.identification,
        email: this.formData.email,
        phone: this.formData.phone,
      });
    }
  }

  public setErrorMessage(message: string): void {
    this.errorMessage = message;
    this.isSubmitting = false;
  }

  onKeyDown(event: KeyboardEvent): void {
    if (event.key === 'Escape') {
      this.onCancel();
    }
  }

  onLogout(): void {
    // Cerrar sesión usando el AuthService
    this.authService.logout();
    // Cerrar el modal
    this.close.emit();
  }
}
