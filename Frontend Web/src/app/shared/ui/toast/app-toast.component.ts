import { Component, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ToastMessage, ToastService } from '@shared/services/toast.service';

@Component({
  selector: 'app-toast',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './app-toast.component.html',
  styleUrl: './app-toast.component.css',
})
export class AppToastComponent {
  private readonly toastService = inject(ToastService);
  readonly toasts$ = this.toastService.toasts$;

  dismiss(id: number): void {
    this.toastService.dismiss(id);
  }

  trackByToastId(_index: number, toast: ToastMessage): number {
    return toast.id;
  }
}
