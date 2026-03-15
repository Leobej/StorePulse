package org.projects.controller;

import org.projects.dto.SalesSummaryResponse;
import org.projects.service.AnalyticsMaterializationService;
import org.projects.service.SalesAnalyticsService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/analytics")
public class AnalyticsController {
    private final SalesAnalyticsService salesAnalyticsService;
    private final AnalyticsMaterializationService analyticsMaterializationService;

    public AnalyticsController(SalesAnalyticsService salesAnalyticsService, AnalyticsMaterializationService analyticsMaterializationService) {
        this.salesAnalyticsService = salesAnalyticsService;
        this.analyticsMaterializationService = analyticsMaterializationService;
    }

    @GetMapping(path = "/sales-summary", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<SalesSummaryResponse> getSalesSummary(@RequestParam UUID batchId) {
        return ResponseEntity.ok(salesAnalyticsService.getSalesSummary(batchId));
    }

    @PostMapping(path = "/recompute", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Void> recomputeAnalytics() {
        analyticsMaterializationService.recomputeAllDailyAggregates();
        return ResponseEntity.accepted().build();
    }
}
