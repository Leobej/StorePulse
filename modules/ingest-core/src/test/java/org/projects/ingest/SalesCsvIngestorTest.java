package org.projects.ingest;

import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class SalesCsvIngestorTest {
    private final SalesCsvIngestor ingestor = new SalesCsvIngestor();

    @Test
    void parsesValidRowsAndCollectsInvalidRows() throws IOException {
        String csv = """
            receipt_id,sold_at,product_sku,product_name,quantity,unit_price,total_amount
            R-1,2026-03-15T10:15:30+02:00,SKU-1,Apple,2,3.50,7.00
            R-2,invalid-date,SKU-2,Banana,1,1.20,1.20
            """;

        IngestResult result = ingestor.ingest(new ByteArrayInputStream(csv.getBytes(StandardCharsets.UTF_8)));

        assertEquals(2, result.rowsRead());
        assertEquals(1, result.validRows());
        assertEquals(1, result.invalidRows());
        assertEquals(1, result.records().size());
        assertEquals(1, result.rowErrors().size());
        assertEquals(3, result.rowErrors().getFirst().lineNumber());
    }

    @Test
    void rejectsMissingRequiredHeaders() {
        String csv = """
            receipt_id,product_sku
            R-1,SKU-1
            """;

        assertThrows(
            InvalidImportFileException.class,
            () -> ingestor.ingest(new ByteArrayInputStream(csv.getBytes(StandardCharsets.UTF_8)))
        );
    }
}
