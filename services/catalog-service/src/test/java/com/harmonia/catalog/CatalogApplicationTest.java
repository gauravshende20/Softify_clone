package com.harmonia.catalog;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

class CatalogApplicationTest {

    @Test
    void applicationClassIsPresent() {
        assertTrue(CatalogApplication.class.getSimpleName().endsWith("Application"));
    }
}
