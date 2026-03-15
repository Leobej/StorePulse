package org.projects.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "sales_daily_aggregate")
public class SalesDailyAggregateEntity {
    @Id
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @Column(name = "business_date", nullable = false, unique = true)
    private LocalDate businessDate;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "store_id", nullable = false)
    private Store store;

    @Column(name = "revenue", nullable = false, precision = 19, scale = 2)
    private BigDecimal revenue;

    @Column(name = "units_sold", nullable = false)
    private int unitsSold;

    @Column(name = "receipts", nullable = false)
    private int receipts;

    @Column(name = "average_basket", nullable = false, precision = 19, scale = 2)
    private BigDecimal averageBasket;

    @Column(name = "top_product_name", length = 255)
    private String topProductName;

    @Column(name = "top_product_units")
    private Integer topProductUnits;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    public SalesDailyAggregateEntity() {
    }

    public SalesDailyAggregateEntity(UUID id, LocalDate businessDate, Store store, BigDecimal revenue, int unitsSold, int receipts, BigDecimal averageBasket, String topProductName, Integer topProductUnits, OffsetDateTime createdAt, OffsetDateTime updatedAt) {
        this.id = id;
        this.businessDate = businessDate;
        this.store = store;
        this.revenue = revenue;
        this.unitsSold = unitsSold;
        this.receipts = receipts;
        this.averageBasket = averageBasket;
        this.topProductName = topProductName;
        this.topProductUnits = topProductUnits;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public LocalDate getBusinessDate() {
        return businessDate;
    }

    public Store getStore() {
        return store;
    }

    public void setStore(Store store) {
        this.store = store;
    }

    public void setBusinessDate(LocalDate businessDate) {
        this.businessDate = businessDate;
    }

    public BigDecimal getRevenue() {
        return revenue;
    }

    public void setRevenue(BigDecimal revenue) {
        this.revenue = revenue;
    }

    public int getUnitsSold() {
        return unitsSold;
    }

    public void setUnitsSold(int unitsSold) {
        this.unitsSold = unitsSold;
    }

    public int getReceipts() {
        return receipts;
    }

    public void setReceipts(int receipts) {
        this.receipts = receipts;
    }

    public BigDecimal getAverageBasket() {
        return averageBasket;
    }

    public void setAverageBasket(BigDecimal averageBasket) {
        this.averageBasket = averageBasket;
    }

    public String getTopProductName() {
        return topProductName;
    }

    public void setTopProductName(String topProductName) {
        this.topProductName = topProductName;
    }

    public Integer getTopProductUnits() {
        return topProductUnits;
    }

    public void setTopProductUnits(Integer topProductUnits) {
        this.topProductUnits = topProductUnits;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(OffsetDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public OffsetDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(OffsetDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
