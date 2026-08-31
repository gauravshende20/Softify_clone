package com.harmonia.playlist;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

class PlaylistApplicationTest {

    @Test
    void applicationClassIsPresent() {
        assertTrue(PlaylistApplication.class.getSimpleName().endsWith("Application"));
    }
}
