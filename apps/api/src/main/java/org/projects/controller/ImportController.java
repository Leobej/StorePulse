package org.projects.controller;

import org.projects.dto.ImportBatchResponse;
import org.projects.service.ImportService;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping("/imports")
public class ImportController {

    private final ImportService importService;

    public ImportController(ImportService importService) {
        this.importService = importService;
    }

    @PostMapping(path = "/sales", consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ImportBatchResponse> uploadedSales(@RequestPart("file") MultipartFile file) throws IOException {
        ImportBatchResponse response = importService.createSalesImport(file);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
