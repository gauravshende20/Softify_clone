package com.harmonia.payment.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.harmonia.common.api.error.ErrorCode;
import com.harmonia.common.api.error.HarmoniaException;
import com.harmonia.payment.config.StripeProperties;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.Map;

@Component
public class StripeGateway {
    private static final URI API = URI.create("https://api.stripe.com/v1/");
    private final StripeProperties properties;
    private final ObjectMapper mapper;
    private final HttpClient client = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(5)).build();
    public StripeGateway(StripeProperties properties, ObjectMapper mapper) { this.properties = properties; this.mapper = mapper; }

    public Map<String, Object> createCheckoutSession(String userId, String email) {
        Map<String, String> form = new LinkedHashMap<>();
        form.put("mode", "subscription");
        form.put("client_reference_id", userId);
        form.put("customer_email", email);
        form.put("line_items[0][price]", required(properties.getPremiumMonthlyPriceId(), "STRIPE_PRICE_ID_PREMIUM_MONTHLY"));
        form.put("line_items[0][quantity]", "1");
        form.put("subscription_data[metadata][harmonia_user_id]", userId);
        form.put("success_url", baseUrl() + "/subscription/success?session_id={CHECKOUT_SESSION_ID}");
        form.put("cancel_url", baseUrl() + "/subscription/cancelled");
        return post("checkout/sessions", form);
    }

    public Map<String, Object> createPortalSession(String customerId) {
        return post("billing_portal/sessions", Map.of("customer", customerId, "return_url", baseUrl() + "/subscription"));
    }

    public Map<String, Object> retrieveSubscription(String subscriptionId) { return get("subscriptions/" + subscriptionId); }

    private Map<String, Object> post(String path, Map<String, String> form) {
        String body = form.entrySet().stream().map(e -> encode(e.getKey()) + "=" + encode(e.getValue())).reduce((a, b) -> a + "&" + b).orElse("");
        return send(HttpRequest.newBuilder(API.resolve(path)).timeout(Duration.ofSeconds(10)).header("Authorization", "Bearer " + required(properties.getSecretKey(), "STRIPE_SECRET_KEY")).header("Content-Type", "application/x-www-form-urlencoded").POST(HttpRequest.BodyPublishers.ofString(body)).build());
    }
    private Map<String, Object> get(String path) { return send(HttpRequest.newBuilder(API.resolve(path)).timeout(Duration.ofSeconds(10)).header("Authorization", "Bearer " + required(properties.getSecretKey(), "STRIPE_SECRET_KEY")).GET().build()); }
    private Map<String, Object> send(HttpRequest request) {
        try {
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() < 200 || response.statusCode() >= 300) throw HarmoniaException.serviceUnavailable(ErrorCode.UPSTREAM_UNAVAILABLE, "Stripe rejected the request (HTTP " + response.statusCode() + ")");
            return mapper.readValue(response.body(), new TypeReference<>() { });
        } catch (HarmoniaException e) { throw e; }
        catch (Exception e) { throw HarmoniaException.serviceUnavailable(ErrorCode.UPSTREAM_UNAVAILABLE, "Stripe is unavailable", e); }
    }
    private String baseUrl() { return required(properties.getPublicUrl(), "APP_PUBLIC_URL").replaceAll("/$", ""); }
    private static String required(String value, String name) { if (value == null || value.isBlank()) throw HarmoniaException.serviceUnavailable(ErrorCode.UPSTREAM_UNAVAILABLE, name + " is not configured"); return value; }
    private static String encode(String value) { return URLEncoder.encode(value, StandardCharsets.UTF_8); }
}
