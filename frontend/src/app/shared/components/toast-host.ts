import { Component, inject } from '@angular/core';
import { ToastService } from '../../core/services/toast';

@Component({
  selector: 'app-toast-host',
  template: `
    <div class="stack" aria-live="polite">
      @for (toast of toasts(); track toast.id) {
        <p class="toast" [class]="toast.tone">{{ toast.message }}</p>
      }
    </div>
  `,
  styles: `
    .stack {
      position: fixed;
      right: 1rem;
      bottom: 7.5rem;
      z-index: 40;
      display: grid;
      gap: 0.5rem;
      width: min(22rem, calc(100vw - 2rem));
    }
    .toast {
      margin: 0;
      padding: 0.8rem 1rem;
      border-radius: 0.7rem;
      background: #242432;
      color: var(--text);
      box-shadow: 0 10px 30px rgba(0, 0, 0, 0.35);
    }
    .success {
      border-left: 3px solid var(--accent);
    }
    .error {
      border-left: 3px solid #e07a6a;
    }
  `,
})
export class ToastHost {
  private readonly toast = inject(ToastService);
  readonly toasts = this.toast.toasts;
}
