package org.projects.ingest;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.OffsetDateTime;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class SalesCsvIngestor {
    private static final Set<String> REQUIRED_HEADERS = Set.of(
        "receipt_id",
        "sold_at",
        "product_sku",
        "product_name",
        "quantity",
        "unit_price",
        "total_amount"
    );

    public IngestResult ingest(InputStream inputStream) throws IOException {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
            String headerLine = reader.readLine();
            if (headerLine == null || headerLine.isBlank()) {
                throw new InvalidImportFileException("CSV file is empty");
            }

            List<String> headerValues = parseCsvLine(headerLine);
            Map<String, Integer> headerIndexes = indexHeaders(headerValues);
            validateHeaders(headerIndexes);

            List<SalesRecord> records = new ArrayList<>();
            List<RowError> rowErrors = new ArrayList<>();
            int rowsRead = 0;
            int validRows = 0;
            int invalidRows = 0;
            int lineNumber = 1;
            String line;

            while ((line = reader.readLine()) != null) {
                lineNumber++;
                if (line.isBlank()) {
                    continue;
                }

                rowsRead++;
                try {
                    List<String> values = parseCsvLine(line);
                    records.add(parseRecord(values, headerIndexes, lineNumber));
                    validRows++;
                } catch (IllegalArgumentException ex) {
                    invalidRows++;
                    rowErrors.add(new RowError(lineNumber, ex.getMessage()));
                }
            }

            return new IngestResult(List.copyOf(records), List.copyOf(rowErrors), rowsRead, validRows, invalidRows);
        }
    }

    private static Map<String, Integer> indexHeaders(List<String> headerValues) {
        Map<String, Integer> headerIndexes = new HashMap<>();
        for (int i = 0; i < headerValues.size(); i++) {
            headerIndexes.put(headerValues.get(i).trim().toLowerCase(), i);
        }
        return headerIndexes;
    }

    private static void validateHeaders(Map<String, Integer> headerIndexes) {
        List<String> missing = REQUIRED_HEADERS.stream()
            .filter(header -> !headerIndexes.containsKey(header))
            .sorted()
            .toList();

        if (!missing.isEmpty()) {
            throw new InvalidImportFileException("Missing required headers: " + String.join(", ", missing));
        }
    }

    private static SalesRecord parseRecord(List<String> values, Map<String, Integer> headerIndexes, int lineNumber) {
        String receiptId = requiredValue(values, headerIndexes, "receipt_id");
        OffsetDateTime soldAt = parseOffsetDateTime(requiredValue(values, headerIndexes, "sold_at"), lineNumber, "sold_at");
        String productSku = requiredValue(values, headerIndexes, "product_sku");
        String productName = requiredValue(values, headerIndexes, "product_name");
        int quantity = parseInteger(requiredValue(values, headerIndexes, "quantity"), lineNumber, "quantity");
        BigDecimal unitPrice = parseDecimal(requiredValue(values, headerIndexes, "unit_price"), lineNumber, "unit_price");
        BigDecimal totalAmount = parseDecimal(requiredValue(values, headerIndexes, "total_amount"), lineNumber, "total_amount");

        return new SalesRecord(receiptId, soldAt, productSku, productName, quantity, unitPrice, totalAmount);
    }

    private static String requiredValue(List<String> values, Map<String, Integer> headerIndexes, String header) {
        Integer index = headerIndexes.get(header);
        if (index == null || index >= values.size()) {
            throw new IllegalArgumentException("Missing value for " + header);
        }

        String value = values.get(index).trim();
        if (value.isEmpty()) {
            throw new IllegalArgumentException("Blank value for " + header);
        }
        return value;
    }

    private static OffsetDateTime parseOffsetDateTime(String value, int lineNumber, String field) {
        try {
            return OffsetDateTime.parse(value);
        } catch (DateTimeParseException ex) {
            throw new IllegalArgumentException("Invalid " + field + " at line " + lineNumber);
        }
    }

    private static int parseInteger(String value, int lineNumber, String field) {
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException("Invalid " + field + " at line " + lineNumber);
        }
    }

    private static BigDecimal parseDecimal(String value, int lineNumber, String field) {
        try {
            return new BigDecimal(value);
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException("Invalid " + field + " at line " + lineNumber);
        }
    }

    private static List<String> parseCsvLine(String line) {
        List<String> values = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        boolean quoted = false;

        for (int i = 0; i < line.length(); i++) {
            char ch = line.charAt(i);
            if (ch == '"') {
                if (quoted && i + 1 < line.length() && line.charAt(i + 1) == '"') {
                    current.append('"');
                    i++;
                } else {
                    quoted = !quoted;
                }
            } else if (ch == ',' && !quoted) {
                values.add(current.toString());
                current.setLength(0);
            } else {
                current.append(ch);
            }
        }

        if (quoted) {
            throw new IllegalArgumentException("Unclosed quoted value");
        }

        values.add(current.toString());
        return values;
    }
}
