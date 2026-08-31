package com.harmonia.recommendation;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

class RecommendationApplicationTest {

    @Test
    void applicationClassIsPresent() {
        assertTrue(RecommendationApplication.class.getSimpleName().endsWith("Application"));
    }
}
