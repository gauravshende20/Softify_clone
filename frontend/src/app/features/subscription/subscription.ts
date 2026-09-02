import { DatePipe } from '@angular/common';
import { Component, inject, signal } from '@angular/core';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { rxResource } from '@angular/core/rxjs-interop';
import { PaymentService } from '../../core/services/payment';
import { ToastService } from '../../core/services/toast';
import { httpMessage } from '../../shared/utils/format';

@Component({
  selector: 'app-subscription',
  imports: [DatePipe, RouterLink],
  templateUrl: './subscription.html',
  styleUrl: './subscription.scss',
})
export class SubscriptionPage {
  private readonly payments = inject(PaymentService);
  private readonly toast = inject(ToastService);
  private readonly route = inject(ActivatedRoute);
  readonly redirecting = signal(false);
  readonly completed = this.route.snapshot.routeConfig?.path === 'subscription/success';
  readonly cancelled = this.route.snapshot.routeConfig?.path === 'subscription/cancelled';
  readonly subscription = rxResource({ stream: () => this.payments.subscription() });

  checkout(): void {
    this.redirecting.set(true);
    this.payments.checkout().subscribe({
      next: ({ checkoutUrl }) => window.location.assign(checkoutUrl),
      error: (error) => { this.redirecting.set(false); this.toast.error(httpMessage(error, 'Could not start checkout')); },
    });
  }

  manage(): void {
    this.redirecting.set(true);
    this.payments.portal().subscribe({
      next: ({ checkoutUrl }) => window.location.assign(checkoutUrl),
      error: (error) => { this.redirecting.set(false); this.toast.error(httpMessage(error, 'Could not open subscription settings')); },
    });
  }
}
