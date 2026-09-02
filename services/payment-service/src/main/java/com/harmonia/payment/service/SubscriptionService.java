package com.harmonia.payment.service;

import com.harmonia.common.api.error.ErrorCode;
import com.harmonia.common.api.error.HarmoniaException;
import com.harmonia.payment.config.StripeProperties;
import com.harmonia.payment.domain.ProcessedStripeEvent;
import com.harmonia.payment.domain.Subscription;
import com.harmonia.payment.dto.CheckoutSessionResponse;
import com.harmonia.payment.dto.SubscriptionResponse;
import com.harmonia.payment.repo.ProcessedStripeEventRepository;
import com.harmonia.payment.repo.SubscriptionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;

@Service
public class SubscriptionService {
    private final SubscriptionRepository subscriptions; private final ProcessedStripeEventRepository events; private final StripeGateway stripe; private final StripeProperties properties;
    public SubscriptionService(SubscriptionRepository subscriptions, ProcessedStripeEventRepository events, StripeGateway stripe, StripeProperties properties) { this.subscriptions = subscriptions; this.events = events; this.stripe = stripe; this.properties = properties; }
    public CheckoutSessionResponse checkout(UUID userId, String email) { return new CheckoutSessionResponse(text(stripe.createCheckoutSession(userId.toString(), email), "url")); }
    public SubscriptionResponse current(UUID userId) { return subscriptions.findByUserId(userId).map(SubscriptionResponse::from).orElseGet(SubscriptionResponse::free); }
    public String portal(UUID userId) { Subscription subscription = subscriptions.findByUserId(userId).orElseThrow(() -> HarmoniaException.badRequest(ErrorCode.BAD_REQUEST, "No subscription to manage")); if (subscription.getStripeCustomerId() == null) throw HarmoniaException.badRequest(ErrorCode.BAD_REQUEST, "Subscription checkout is not complete"); return text(stripe.createPortalSession(subscription.getStripeCustomerId()), "url"); }
    @Transactional
    public void processWebhook(Map<String, Object> event) {
        String eventId = text(event, "id"); if (events.existsById(eventId)) return;
        String type = text(event, "type"); Map<String, Object> object = object(object(event, "data"), "object");
        if ("checkout.session.completed".equals(type)) synchronizeCheckout(object);
        else if (type.startsWith("customer.subscription.")) synchronizeSubscription(object);
        events.save(new ProcessedStripeEvent(eventId));
    }
    private void synchronizeCheckout(Map<String, Object> session) {
        String subscriptionId = textOrNull(session, "subscription"); if (subscriptionId == null) return;
        synchronize(subscriptionId, UUID.fromString(text(session, "client_reference_id")), textOrNull(session, "customer"));
    }
    private void synchronizeSubscription(Map<String, Object> stripeSubscription) {
        String id = text(stripeSubscription, "id"); UUID userId = subscriptions.findByStripeSubscriptionId(id).map(Subscription::getUserId).orElseGet(() -> UUID.fromString(text(object(stripeSubscription, "metadata"), "harmonia_user_id")));
        synchronize(stripeSubscription, userId, textOrNull(stripeSubscription, "customer"));
    }
    private void synchronize(String id, UUID userId, String customerId) { synchronize(stripe.retrieveSubscription(id), userId, customerId); }
    private void synchronize(Map<String, Object> remote, UUID userId, String customerId) {
        Subscription local = subscriptions.findByUserId(userId).orElseGet(() -> Subscription.create(userId, requiredPriceId()));
        local.synchronize(customerId, text(remote, "id"), text(remote, "status"), epoch(textOrNull(remote, "current_period_end")), Boolean.TRUE.equals(remote.get("cancel_at_period_end"))); subscriptions.save(local);
    }
    private String requiredPriceId() { if (properties.getPremiumMonthlyPriceId() == null || properties.getPremiumMonthlyPriceId().isBlank()) throw HarmoniaException.serviceUnavailable(ErrorCode.UPSTREAM_UNAVAILABLE, "STRIPE_PRICE_ID_PREMIUM_MONTHLY is not configured"); return properties.getPremiumMonthlyPriceId(); }
    private static Instant epoch(String seconds) { return seconds == null ? null : Instant.ofEpochSecond(Long.parseLong(seconds)); }
    @SuppressWarnings("unchecked") private static Map<String, Object> object(Map<String, Object> value, String key) { Object result = value.get(key); if (!(result instanceof Map<?, ?>)) throw HarmoniaException.badRequest(ErrorCode.BAD_REQUEST, "Malformed Stripe event"); return (Map<String, Object>) result; }
    private static String text(Map<String, Object> value, String key) { String result = textOrNull(value, key); if (result == null || result.isBlank()) throw HarmoniaException.badRequest(ErrorCode.BAD_REQUEST, "Malformed Stripe event"); return result; }
    private static String textOrNull(Map<String, Object> value, String key) { Object result = value.get(key); return result == null ? null : String.valueOf(result); }
}
