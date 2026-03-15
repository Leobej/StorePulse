CREATE TABLE sales_daily_aggregate (
    id UUID PRIMARY KEY,
    business_date DATE NOT NULL,
    revenue NUMERIC(19, 2) NOT NULL,
    units_sold INTEGER NOT NULL,
    receipts INTEGER NOT NULL,
    average_basket NUMERIC(19, 2) NOT NULL,
    top_product_name VARCHAR(255),
    top_product_units INTEGER,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE UNIQUE INDEX uk_sales_daily_aggregate_business_date ON sales_daily_aggregate(business_date);

CREATE TABLE alert_record (
    id UUID PRIMARY KEY,
    type VARCHAR(64) NOT NULL,
    severity VARCHAR(32) NOT NULL,
    status VARCHAR(32) NOT NULL,
    business_date DATE NOT NULL,
    message VARCHAR(1000) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    acknowledged_at TIMESTAMP WITH TIME ZONE
);

CREATE INDEX idx_alert_record_status_created_at ON alert_record(status, created_at);
