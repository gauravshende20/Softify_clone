export interface Subscription {
  plan: 'FREE' | 'PREMIUM_MONTHLY';
  status: string;
  premium: boolean;
  currentPeriodEnd: string | null;
  cancelAtPeriodEnd: boolean;
}

export interface CheckoutSession {
  checkoutUrl: string;
}
