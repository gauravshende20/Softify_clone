package com.harmonia.payment.dto;
import com.harmonia.payment.domain.Subscription;
import java.time.Instant;
public record SubscriptionResponse(String plan, String status, boolean premium, Instant currentPeriodEnd, boolean cancelAtPeriodEnd) {
    public static SubscriptionResponse free() { return new SubscriptionResponse("FREE", "NONE", false, null, false); }
    public static SubscriptionResponse from(Subscription value) { return new SubscriptionResponse("PREMIUM_MONTHLY", value.getStatus().name(), value.getStatus().grantsPremium(), value.getCurrentPeriodEnd(), value.isCancelAtPeriodEnd()); }
}
