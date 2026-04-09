import { Injectable } from '@angular/core';
import { BehaviorSubject } from 'rxjs';

export type ToastType = 'info' | 'success' | 'warning' | 'error';

export interface ToastMessage {
  id: number;
  text: string;
  type: ToastType;
  duration: number;
}

@Injectable({
  providedIn: 'root',
})
export class ToastService {
  private readonly toastsSubject = new BehaviorSubject<ToastMessage[]>([]);
  readonly toasts$ = this.toastsSubject.asObservable();

  private nextId = 1;
  private readonly timers = new Map<number, ReturnType<typeof setTimeout>>();

  show(text: string, type: ToastType = 'info', duration = 3500): number {
    const toast: ToastMessage = {
      id: this.nextId++,
      text,
      type,
      duration,
    };

    this.toastsSubject.next([...this.toastsSubject.value, toast]);

    if (duration > 0) {
      const timerId = setTimeout(() => this.dismiss(toast.id), duration);
      this.timers.set(toast.id, timerId);
    }

    return toast.id;
  }

  info(text: string, duration?: number): number {
    return this.show(text, 'info', duration);
  }

  success(text: string, duration?: number): number {
    return this.show(text, 'success', duration);
  }

  warning(text: string, duration?: number): number {
    return this.show(text, 'warning', duration);
  }

  error(text: string, duration?: number): number {
    return this.show(text, 'error', duration);
  }

  dismiss(id: number): void {
    const timerId = this.timers.get(id);
    if (timerId) {
      clearTimeout(timerId);
      this.timers.delete(id);
    }

    this.toastsSubject.next(this.toastsSubject.value.filter(t => t.id !== id));
  }

  clear(): void {
    this.timers.forEach(timerId => clearTimeout(timerId));
    this.timers.clear();
    this.toastsSubject.next([]);
  }
}

