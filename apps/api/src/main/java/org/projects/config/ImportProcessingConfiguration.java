package org.projects.config;

import org.projects.analytics.DailySalesAnalyticsService;
import org.projects.analytics.SalesSummaryCalculator;
import org.projects.eventbus.EventBus;
import org.projects.eventbus.InMemoryEventBus;
import org.projects.ingest.SalesCsvIngestor;
import org.projects.processing.InMemoryJobProcessor;
import org.projects.processing.JobProcessor;
import org.projects.scheduler.RecurringTaskScheduler;
import org.projects.scheduler.SingleNodeRecurringTaskScheduler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ImportProcessingConfiguration {
    @Bean(destroyMethod = "close")
    public JobProcessor jobProcessor() {
        return new InMemoryJobProcessor(2);
    }

    @Bean
    public SalesCsvIngestor salesCsvIngestor() {
        return new SalesCsvIngestor();
    }

    @Bean
    public SalesSummaryCalculator salesSummaryCalculator() {
        return new SalesSummaryCalculator();
    }

    @Bean
    public DailySalesAnalyticsService dailySalesAnalyticsService(SalesSummaryCalculator salesSummaryCalculator) {
        return new DailySalesAnalyticsService(salesSummaryCalculator);
    }

    @Bean
    public EventBus eventBus() {
        return new InMemoryEventBus();
    }

    @Bean(destroyMethod = "close")
    public RecurringTaskScheduler recurringTaskScheduler() {
        return new SingleNodeRecurringTaskScheduler(1);
    }
}
