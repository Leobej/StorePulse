package org.projects.controller;

import org.projects.dto.AlertResponse;
import org.projects.service.AlertService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/alerts")
public class AlertController {
    private final AlertService alertService;

    public AlertController(AlertService alertService) {
        this.alertService = alertService;
    }

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<AlertResponse>> getAlerts() {
        return ResponseEntity.ok(alertService.getAlerts());
    }

    @PostMapping(path = "/{alertId}/acknowledge", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<AlertResponse> acknowledgeAlert(@PathVariable UUID alertId) {
        return ResponseEntity.ok(alertService.acknowledgeAlert(alertId));
    }
}
