package com.harmonia.payment.web;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.harmonia.common.security.CurrentUser;
import com.harmonia.payment.dto.CheckoutSessionResponse;
import com.harmonia.payment.dto.SubscriptionResponse;
import com.harmonia.payment.service.StripeWebhookVerifier;
import com.harmonia.payment.service.SubscriptionService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import java.nio.charset.StandardCharsets;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/payments")
public class PaymentController {
    private final SubscriptionService subscriptions; private final StripeWebhookVerifier verifier; private final ObjectMapper mapper;
    public PaymentController(SubscriptionService subscriptions, StripeWebhookVerifier verifier, ObjectMapper mapper) { this.subscriptions = subscriptions; this.verifier = verifier; this.mapper = mapper; }
    @PostMapping("/checkout") public CheckoutSessionResponse checkout(CurrentUser user) { return subscriptions.checkout(user.id(), user.email()); }
    @GetMapping("/subscription") public SubscriptionResponse subscription(CurrentUser user) { return subscriptions.current(user.id()); }
    @PostMapping("/portal") public CheckoutSessionResponse portal(CurrentUser user) { return new CheckoutSessionResponse(subscriptions.portal(user.id())); }
    @PostMapping("/webhooks/stripe") @ResponseStatus(HttpStatus.NO_CONTENT)
    public void stripeWebhook(@RequestHeader("Stripe-Signature") String signature, @RequestBody byte[] body) throws Exception {
        String payload = new String(body, StandardCharsets.UTF_8); verifier.verify(payload, signature);
        subscriptions.processWebhook(mapper.readValue(payload, new TypeReference<Map<String, Object>>() { }));
    }
}
