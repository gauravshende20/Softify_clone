package com.harmonia.payment.service;

import com.harmonia.common.api.error.ErrorCode;
import com.harmonia.common.api.error.HarmoniaException;
import com.harmonia.payment.config.StripeProperties;
import org.springframework.stereotype.Component;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Instant;

@Component
public class StripeWebhookVerifier {
    private final StripeProperties properties;
    public StripeWebhookVerifier(StripeProperties properties) { this.properties = properties; }
    public void verify(String payload, String signature) {
        try {
            String timestamp = null, signatureV1 = null;
            for (String part : signature.split(",")) { String[] pair = part.split("=", 2); if (pair.length == 2 && pair[0].equals("t")) timestamp = pair[1]; if (pair.length == 2 && pair[0].equals("v1")) signatureV1 = pair[1]; }
            if (timestamp == null || signatureV1 == null || Math.abs(Instant.now().getEpochSecond() - Long.parseLong(timestamp)) > 300) throw HarmoniaException.badRequest(ErrorCode.BAD_REQUEST, "Invalid Stripe webhook signature");
            Mac mac = Mac.getInstance("HmacSHA256"); mac.init(new SecretKeySpec(requiredSecret().getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            byte[] expected = mac.doFinal((timestamp + "." + payload).getBytes(StandardCharsets.UTF_8));
            byte[] received = java.util.HexFormat.of().parseHex(signatureV1);
            if (!MessageDigest.isEqual(expected, received)) throw HarmoniaException.badRequest(ErrorCode.BAD_REQUEST, "Invalid Stripe webhook signature");
        } catch (HarmoniaException e) { throw e; } catch (Exception e) { throw HarmoniaException.badRequest(ErrorCode.BAD_REQUEST, "Invalid Stripe webhook signature"); }
    }
    private String requiredSecret() { if (properties.getWebhookSecret() == null || properties.getWebhookSecret().isBlank()) throw HarmoniaException.serviceUnavailable(ErrorCode.UPSTREAM_UNAVAILABLE, "STRIPE_WEBHOOK_SECRET is not configured"); return properties.getWebhookSecret(); }
}
