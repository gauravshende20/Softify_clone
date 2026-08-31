import { Service, signal } from '@angular/core';

export interface Toast {
  id: number;
  message: string;
  tone: 'info' | 'success' | 'error';
}

@Service()
export class ToastService {
  private seq = 0;
  readonly toasts = signal<Toast[]>([]);

  show(message: string, tone: Toast['tone'] = 'info'): void {
    const id = ++this.seq;
    this.toasts.update((list) => [...list, { id, message, tone }]);
    window.setTimeout(() => this.dismiss(id), 4200);
  }

  success(message: string): void {
    this.show(message, 'success');
  }

  error(message: string): void {
    this.show(message, 'error');
  }

  dismiss(id: number): void {
    this.toasts.update((list) => list.filter((t) => t.id !== id));
  }
}
