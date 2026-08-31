package com.harmonia.search.web;

import com.harmonia.common.kafka.DomainEvent;
import com.harmonia.common.kafka.DomainEventPublisher;
import com.harmonia.common.kafka.EventType;
import com.harmonia.common.kafka.Topics;
import com.harmonia.common.security.CurrentUser;
import com.harmonia.search.dto.GroupedSearchResponse;
import com.harmonia.search.service.SearchQueryService;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.slf4j.MDC;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@Validated
@RestController
@RequestMapping("/api/v1/search")
public class SearchController {

    private final SearchQueryService searchQueryService;
    private final DomainEventPublisher events;

    public SearchController(SearchQueryService searchQueryService, DomainEventPublisher events) {
        this.searchQueryService = searchQueryService;
        this.events = events;
    }

    @GetMapping
    public GroupedSearchResponse search(
            @RequestParam String q,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String genre,
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(50) int size,
            CurrentUser user
    ) {
        GroupedSearchResponse response = searchQueryService.search(q, type, genre, page, size);
        publishSearchPerformed(q, type, genre, "search", response.totalHits(), user);
        return response;
    }

    @GetMapping("/suggest")
    public GroupedSearchResponse suggest(@RequestParam String q, CurrentUser user) {
        GroupedSearchResponse response = searchQueryService.suggest(q);
        publishSearchPerformed(q, null, null, "suggest", response.totalHits(), user);
        return response;
    }

    private void publishSearchPerformed(String q, String type, String genre, String kind, int resultCount, CurrentUser user) {
        String userId = user == null || user.id() == null ? "anonymous" : user.id().toString();
        Map<String, Object> payload = new HashMap<>();
        payload.put("q", q);
        payload.put("kind", kind);
        payload.put("resultCount", resultCount);
        if (type != null) {
            payload.put("type", type);
        }
        if (genre != null) {
            payload.put("genre", genre);
        }
        events.publish(Topics.SEARCH, DomainEvent.of(
                EventType.SEARCH_PERFORMED, "Search", userId, "search-service",
                MDC.get("traceId"), userId, payload));
    }
}
