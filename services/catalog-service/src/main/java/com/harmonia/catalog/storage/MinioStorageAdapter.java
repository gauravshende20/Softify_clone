package com.harmonia.catalog.storage;

import com.harmonia.catalog.config.StorageProperties;
import com.harmonia.common.api.error.ErrorCode;
import com.harmonia.common.api.error.HarmoniaException;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.timelimiter.TimeLimiter;
import io.github.resilience4j.timelimiter.TimeLimiterRegistry;
import io.minio.GetPresignedObjectUrlArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.http.Method;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.time.Duration;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Component
public class MinioStorageAdapter implements StoragePort {

    private final MinioClient minioClient;
    private final StorageProperties properties;
    private final CircuitBreaker circuitBreaker;
    private final TimeLimiter timeLimiter;
    private final ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor();

    public MinioStorageAdapter(MinioClient minioClient,
                               StorageProperties properties,
                               CircuitBreakerRegistry circuitBreakerRegistry,
                               TimeLimiterRegistry timeLimiterRegistry) {
        this.minioClient = minioClient;
        this.properties = properties;
        this.circuitBreaker = circuitBreakerRegistry.circuitBreaker("minio");
        this.timeLimiter = timeLimiterRegistry.timeLimiter("minio");
    }

    @Override
    public void putAudio(String objectKey, InputStream content, long size, String contentType) {
        execute(() -> {
            put(properties.getBucketAudio(), objectKey, content, size, contentType);
            return null;
        });
    }

    @Override
    public void putArtwork(String objectKey, InputStream content, long size, String contentType) {
        execute(() -> {
            put(properties.getBucketArtwork(), objectKey, content, size, contentType);
            return null;
        });
    }

    @Override
    public String presignedAudioUrl(String objectKey, Duration expiry) {
        return execute(() -> minioClient.getPresignedObjectUrl(
                GetPresignedObjectUrlArgs.builder()
                        .method(Method.GET)
                        .bucket(properties.getBucketAudio())
                        .object(objectKey)
                        .expiry((int) expiry.toSeconds())
                        .build()));
    }

    private void put(String bucket, String objectKey, InputStream content, long size, String contentType) throws Exception {
        minioClient.putObject(PutObjectArgs.builder()
                .bucket(bucket)
                .object(objectKey)
                .stream(content, size, -1)
                .contentType(contentType)
                .build());
    }

    private <T> T execute(Callable<T> action) {
        Callable<T> decorated = CircuitBreaker.decorateCallable(circuitBreaker, action);
        try {
            return timeLimiter.executeFutureSupplier(() -> executor.submit(decorated));
        } catch (HarmoniaException ex) {
            throw ex;
        } catch (Exception ex) {
            Throwable cause = ex.getCause() == null ? ex : ex.getCause();
            if (cause instanceof HarmoniaException he) {
                throw he;
            }
            throw new HarmoniaException(ErrorCode.STORAGE_FAILURE, 503, "Storage operation failed", cause);
        }
    }
}
