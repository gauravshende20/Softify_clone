package com.harmonia.catalog.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "harmonia.storage")
public class StorageProperties {

    private String endpoint = "http://localhost:9000";
    private String accessKey;
    private String secretKey;
    private String bucketAudio = "harmonia-audio";
    private String bucketArtwork = "harmonia-artwork";
    private String region = "us-east-1";

    public String getEndpoint() {
        return endpoint;
    }

    public void setEndpoint(String endpoint) {
        this.endpoint = endpoint;
    }

    public String getAccessKey() {
        return accessKey;
    }

    public void setAccessKey(String accessKey) {
        this.accessKey = accessKey;
    }

    public String getSecretKey() {
        return secretKey;
    }

    public void setSecretKey(String secretKey) {
        this.secretKey = secretKey;
    }

    public String getBucketAudio() {
        return bucketAudio;
    }

    public void setBucketAudio(String bucketAudio) {
        this.bucketAudio = bucketAudio;
    }

    public String getBucketArtwork() {
        return bucketArtwork;
    }

    public void setBucketArtwork(String bucketArtwork) {
        this.bucketArtwork = bucketArtwork;
    }

    public String getRegion() {
        return region;
    }

    public void setRegion(String region) {
        this.region = region;
    }
}
