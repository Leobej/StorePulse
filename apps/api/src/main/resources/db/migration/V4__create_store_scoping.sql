CREATE TABLE store (
    id UUID PRIMARY KEY,
    code VARCHAR(64) NOT NULL,
    name VARCHAR(255) NOT NULL,
    active BOOLEAN NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE UNIQUE INDEX uk_store_code ON store(code);

INSERT INTO store (id, code, name, active, created_at, updated_at)
VALUES (
    '11111111-1111-1111-1111-111111111111',
    'default-store',
    'Default Store',
    TRUE,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
);

ALTER TABLE app_user ADD COLUMN store_id UUID;
UPDATE app_user SET store_id = '11111111-1111-1111-1111-111111111111' WHERE store_id IS NULL;
ALTER TABLE app_user ALTER COLUMN store_id SET NOT NULL;
ALTER TABLE app_user ADD CONSTRAINT fk_app_user_store FOREIGN KEY (store_id) REFERENCES store(id);

ALTER TABLE import_batch ADD COLUMN store_id UUID;
UPDATE import_batch SET store_id = '11111111-1111-1111-1111-111111111111' WHERE store_id IS NULL;
ALTER TABLE import_batch ALTER COLUMN store_id SET NOT NULL;
ALTER TABLE import_batch ADD CONSTRAINT fk_import_batch_store FOREIGN KEY (store_id) REFERENCES store(id);
DROP INDEX uk_import_batch_type_checksum;
CREATE UNIQUE INDEX uk_import_batch_store_type_checksum ON import_batch(store_id, type, file_checksum);

ALTER TABLE sales_record ADD COLUMN store_id UUID;
UPDATE sales_record SET store_id = '11111111-1111-1111-1111-111111111111' WHERE store_id IS NULL;
ALTER TABLE sales_record ALTER COLUMN store_id SET NOT NULL;
ALTER TABLE sales_record ADD CONSTRAINT fk_sales_record_store FOREIGN KEY (store_id) REFERENCES store(id);
CREATE INDEX idx_sales_record_store_sold_at ON sales_record(store_id, sold_at);

ALTER TABLE sales_daily_aggregate ADD COLUMN store_id UUID;
UPDATE sales_daily_aggregate SET store_id = '11111111-1111-1111-1111-111111111111' WHERE store_id IS NULL;
ALTER TABLE sales_daily_aggregate ALTER COLUMN store_id SET NOT NULL;
ALTER TABLE sales_daily_aggregate ADD CONSTRAINT fk_sales_daily_aggregate_store FOREIGN KEY (store_id) REFERENCES store(id);
DROP INDEX uk_sales_daily_aggregate_business_date;
CREATE UNIQUE INDEX uk_sales_daily_aggregate_store_business_date ON sales_daily_aggregate(store_id, business_date);

ALTER TABLE alert_record ADD COLUMN store_id UUID;
UPDATE alert_record SET store_id = '11111111-1111-1111-1111-111111111111' WHERE store_id IS NULL;
ALTER TABLE alert_record ALTER COLUMN store_id SET NOT NULL;
ALTER TABLE alert_record ADD CONSTRAINT fk_alert_record_store FOREIGN KEY (store_id) REFERENCES store(id);
CREATE INDEX idx_alert_record_store_status_created_at ON alert_record(store_id, status, created_at);
