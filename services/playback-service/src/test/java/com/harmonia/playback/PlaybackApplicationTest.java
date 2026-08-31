package com.harmonia.playback;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

class PlaybackApplicationTest {

    @Test
    void applicationClassIsPresent() {
        assertTrue(PlaybackApplication.class.getSimpleName().endsWith("Application"));
    }
}
