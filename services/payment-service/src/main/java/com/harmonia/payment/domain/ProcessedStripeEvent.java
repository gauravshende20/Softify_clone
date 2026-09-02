package com.harmonia.payment.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;

@Entity @Table(name = "processed_stripe_events")
public class ProcessedStripeEvent {
    @Id private String stripeEventId;
    private Instant processedAt;
    protected ProcessedStripeEvent() { }
    public ProcessedStripeEvent(String stripeEventId) { this.stripeEventId = stripeEventId; this.processedAt = Instant.now(); }
}
