package com.harmonia.payment.domain;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "subscriptions")
public class Subscription {
    @Id @Column(columnDefinition = "char(36)") private UUID id;
    @Column(name = "user_id", nullable = false, unique = true, columnDefinition = "char(36)") private UUID userId;
    @Column(name = "stripe_customer_id") private String stripeCustomerId;
    @Column(name = "stripe_subscription_id", unique = true) private String stripeSubscriptionId;
    @Column(name = "stripe_price_id", nullable = false) private String stripePriceId;
    @Enumerated(EnumType.STRING) @Column(nullable = false) private SubscriptionStatus status;
    @Column(name = "current_period_end") private Instant currentPeriodEnd;
    @Column(name = "cancel_at_period_end", nullable = false) private boolean cancelAtPeriodEnd;
    @Column(name = "created_at", nullable = false) private Instant createdAt;
    @Column(name = "updated_at", nullable = false) private Instant updatedAt;
    protected Subscription() { }
    public static Subscription create(UUID userId, String priceId) { Subscription s = new Subscription(); s.id = UUID.randomUUID(); s.userId = userId; s.stripePriceId = priceId; s.status = SubscriptionStatus.INCOMPLETE; s.createdAt = s.updatedAt = Instant.now(); return s; }
    public void synchronize(String customerId, String subscriptionId, String status, Instant periodEnd, boolean cancelAtPeriodEnd) { this.stripeCustomerId = customerId; this.stripeSubscriptionId = subscriptionId; this.status = SubscriptionStatus.fromStripe(status); this.currentPeriodEnd = periodEnd; this.cancelAtPeriodEnd = cancelAtPeriodEnd; this.updatedAt = Instant.now(); }
    public UUID getUserId() { return userId; } public String getStripeCustomerId() { return stripeCustomerId; } public String getStripeSubscriptionId() { return stripeSubscriptionId; } public SubscriptionStatus getStatus() { return status; } public Instant getCurrentPeriodEnd() { return currentPeriodEnd; } public boolean isCancelAtPeriodEnd() { return cancelAtPeriodEnd; }
}
