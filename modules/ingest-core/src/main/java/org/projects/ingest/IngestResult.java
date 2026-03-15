package org.projects.ingest;

import java.util.List;

public record IngestResult(
    List<SalesRecord> records,
    List<RowError> rowErrors,
    int rowsRead,
    int validRows,
    int invalidRows
) {
}
