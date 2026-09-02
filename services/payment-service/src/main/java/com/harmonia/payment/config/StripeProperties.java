package com.harmonia.payment.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("harmonia.payments.stripe")
public class StripeProperties {
    private String secretKey;
    private String webhookSecret;
    private String premiumMonthlyPriceId;
    private String publicUrl;
    public String getSecretKey() { return secretKey; }
    public void setSecretKey(String secretKey) { this.secretKey = secretKey; }
    public String getWebhookSecret() { return webhookSecret; }
    public void setWebhookSecret(String webhookSecret) { this.webhookSecret = webhookSecret; }
    public String getPremiumMonthlyPriceId() { return premiumMonthlyPriceId; }
    public void setPremiumMonthlyPriceId(String premiumMonthlyPriceId) { this.premiumMonthlyPriceId = premiumMonthlyPriceId; }
    public String getPublicUrl() { return publicUrl; }
    public void setPublicUrl(String publicUrl) { this.publicUrl = publicUrl; }
}
