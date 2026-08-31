package com.harmonia.analytics.web;

import com.harmonia.analytics.dto.AnalyticsOverviewResponse;
import com.harmonia.analytics.dto.PopularTrackResponse;
import com.harmonia.analytics.dto.RecentEventsResponse;
import com.harmonia.analytics.service.AnalyticsService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

@RestController
@RequestMapping("/api/v1/analytics")
@PreAuthorize("hasRole('ADMIN')")
public class AnalyticsController {

    private final AnalyticsService analyticsService;

    public AnalyticsController(AnalyticsService analyticsService) {
        this.analyticsService = analyticsService;
    }

    @GetMapping("/overview")
    public AnalyticsOverviewResponse overview() {
        return analyticsService.overview();
    }

    @GetMapping("/tracks/popular")
    public List<PopularTrackResponse> popularTracks(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant to) {
        Instant end = to != null ? to : Instant.now();
        Instant start = from != null ? from : end.minus(7, ChronoUnit.DAYS);
        return analyticsService.popularTracks(start, end);
    }

    @GetMapping("/recent")
    public RecentEventsResponse recent() {
        return analyticsService.recent();
    }
}
