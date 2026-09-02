import { HttpClient } from '@angular/common/http';
import { inject, Service } from '@angular/core';
import { Observable } from 'rxjs';
import { API_BASE } from '../models/api';
import { CheckoutSession, Subscription } from '../models/payment';

@Service()
export class PaymentService {
  private readonly http = inject(HttpClient);

  subscription(): Observable<Subscription> {
    return this.http.get<Subscription>(`${API_BASE}/payments/subscription`);
  }

  checkout(): Observable<CheckoutSession> {
    return this.http.post<CheckoutSession>(`${API_BASE}/payments/checkout`, {});
  }

  portal(): Observable<CheckoutSession> {
    return this.http.post<CheckoutSession>(`${API_BASE}/payments/portal`, {});
  }
}
