CREATE TABLE import_batch (
    id UUID PRIMARY KEY,
    type VARCHAR(32) NOT NULL,
    status VARCHAR(32) NOT NULL,
    file_path VARCHAR(1024) NOT NULL,
    original_file_name VARCHAR(255) NOT NULL,
    file_checksum VARCHAR(64) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE,
    error_message VARCHAR(2000),
    processed_rows INTEGER NOT NULL DEFAULT 0,
    successful_rows INTEGER NOT NULL DEFAULT 0,
    failed_rows INTEGER NOT NULL DEFAULT 0
);

CREATE UNIQUE INDEX uk_import_batch_type_checksum ON import_batch(type, file_checksum);

CREATE TABLE sales_record (
    id UUID PRIMARY KEY,
    import_batch_id UUID NOT NULL,
    receipt_id VARCHAR(128) NOT NULL,
    sold_at TIMESTAMP WITH TIME ZONE NOT NULL,
    product_sku VARCHAR(128) NOT NULL,
    product_name VARCHAR(255) NOT NULL,
    quantity INTEGER NOT NULL,
    unit_price NUMERIC(19, 2) NOT NULL,
    total_amount NUMERIC(19, 2) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT fk_sales_record_import_batch FOREIGN KEY (import_batch_id) REFERENCES import_batch(id)
);

CREATE INDEX idx_sales_record_import_batch ON sales_record(import_batch_id);

CREATE TABLE import_row_error (
    id UUID PRIMARY KEY,
    import_batch_id UUID NOT NULL,
    line_number INTEGER NOT NULL,
    error_message VARCHAR(1000) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT fk_import_row_error_import_batch FOREIGN KEY (import_batch_id) REFERENCES import_batch(id)
);

CREATE INDEX idx_import_row_error_import_batch ON import_row_error(import_batch_id);
