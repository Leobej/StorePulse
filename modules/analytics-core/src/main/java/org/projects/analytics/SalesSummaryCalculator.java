package org.projects.analytics;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class SalesSummaryCalculator {
    public SalesSummary summarize(List<SalesFact> facts) {
        BigDecimal revenue = facts.stream()
            .map(SalesFact::totalAmount)
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        int unitsSold = facts.stream()
            .mapToInt(SalesFact::quantity)
            .sum();

        int receipts = Set.copyOf(
            facts.stream()
                .map(SalesFact::receiptId)
                .collect(Collectors.toList())
        ).size();

        BigDecimal averageBasket = receipts == 0
            ? BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP)
            : revenue.divide(BigDecimal.valueOf(receipts), 2, RoundingMode.HALF_UP);

        Map<String, Integer> unitsByProduct = facts.stream()
            .collect(Collectors.groupingBy(SalesFact::productName, Collectors.summingInt(SalesFact::quantity)));

        List<TopProduct> topProducts = unitsByProduct.entrySet().stream()
            .map(entry -> new TopProduct(entry.getKey(), entry.getValue()))
            .sorted(Comparator.comparingInt(TopProduct::unitsSold).reversed().thenComparing(TopProduct::productName))
            .limit(5)
            .toList();

        Map<Integer, BigDecimal> revenueByHour = facts.stream()
            .collect(Collectors.groupingBy(
                fact -> fact.soldAt().getHour(),
                Collectors.reducing(BigDecimal.ZERO, SalesFact::totalAmount, BigDecimal::add)
            ));

        List<HourlySales> hourlyDistribution = new ArrayList<>();
        for (int hour = 0; hour < 24; hour++) {
            hourlyDistribution.add(new HourlySales(
                hour,
                revenueByHour.getOrDefault(hour, BigDecimal.ZERO).setScale(2, RoundingMode.HALF_UP)
            ));
        }

        return new SalesSummary(
            revenue.setScale(2, RoundingMode.HALF_UP),
            unitsSold,
            receipts,
            averageBasket,
            topProducts,
            List.copyOf(hourlyDistribution)
        );
    }
}
