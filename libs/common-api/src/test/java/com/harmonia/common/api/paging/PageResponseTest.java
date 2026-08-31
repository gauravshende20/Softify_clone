package com.harmonia.common.api.paging;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PageResponseTest {

    @Test
    void computesTotalPagesAndFlags() {
        PageResponse<String> page = PageResponse.of(List.of("a", "b"), 0, 2, 5);
        assertEquals(3, page.totalPages());
        assertTrue(page.first());
        assertEquals(2, page.content().size());
    }
}
