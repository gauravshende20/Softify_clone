package com.harmonia.payment.domain;

public enum SubscriptionStatus {
    INCOMPLETE, INCOMPLETE_EXPIRED, TRIALING, ACTIVE, PAST_DUE, CANCELED, UNPAID, PAUSED;
    public boolean grantsPremium() { return this == ACTIVE || this == TRIALING; }
    public static SubscriptionStatus fromStripe(String value) { return SubscriptionStatus.valueOf(value.toUpperCase().replace('-', '_')); }
}
