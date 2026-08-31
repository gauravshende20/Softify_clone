package com.harmonia.search;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

class SearchApplicationTest {

    @Test
    void applicationClassIsPresent() {
        assertTrue(SearchApplication.class.getSimpleName().endsWith("Application"));
    }
}
