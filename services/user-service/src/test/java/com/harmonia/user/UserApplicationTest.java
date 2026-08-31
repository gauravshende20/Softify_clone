package com.harmonia.user;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

class UserApplicationTest {

    @Test
    void applicationClassIsPresent() {
        assertTrue(UserApplication.class.getSimpleName().endsWith("Application"));
    }
}
