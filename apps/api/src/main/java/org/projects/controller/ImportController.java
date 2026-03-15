package org.projects.controller;

import org.projects.dto.ImportBatchResponse;
import org.projects.dto.ImportRowErrorResponse;
import org.projects.dto.SalesRecordResponse;
import org.projects.service.ImportService;
import org.projects.service.SalesQueryService;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.UUID;

@RestController
@RequestMapping("/imports")
public class ImportController {
    private final ImportService importService;
    private final SalesQueryService salesQueryService;

    public ImportController(ImportService importService, SalesQueryService salesQueryService) {
        this.importService = importService;
        this.salesQueryService = salesQueryService;
    }

    @PostMapping(path = "/sales", consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ImportBatchResponse> uploadedSales(@RequestPart("file") MultipartFile file) throws IOException {
        ImportBatchResponse response = importService.createSalesImport(file);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping(path = "/{batchId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ImportBatchResponse> getImport(@PathVariable UUID batchId) {
        return ResponseEntity.ok(importService.getImportBatch(batchId));
    }

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<java.util.List<ImportBatchResponse>> listImports() {
        return ResponseEntity.ok(importService.listImports());
    }

    @GetMapping(path = "/{batchId}/sales-records", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<java.util.List<SalesRecordResponse>> getSalesRecords(@PathVariable UUID batchId) {
        return ResponseEntity.ok(salesQueryService.getSalesRecords(batchId));
    }

    @GetMapping(path = "/{batchId}/row-errors", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<java.util.List<ImportRowErrorResponse>> getRowErrors(@PathVariable UUID batchId) {
        return ResponseEntity.ok(salesQueryService.getRowErrors(batchId));
    }
}
