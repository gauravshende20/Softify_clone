package com.harmonia.catalog.storage;

import java.io.InputStream;
import java.time.Duration;

public interface StoragePort {

    void putAudio(String objectKey, InputStream content, long size, String contentType);

    String presignedAudioUrl(String objectKey, Duration expiry);

    void putArtwork(String objectKey, InputStream content, long size, String contentType);
}
