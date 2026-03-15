package org.projects.service;

import org.projects.domain.AlertStatus;
import org.projects.dto.AlertResponse;
import org.projects.persistence.entity.AlertRecord;
import org.projects.repository.AlertRecordRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

import static org.springframework.http.HttpStatus.NOT_FOUND;

@Service
public class AlertService {
    private final AlertRecordRepository alertRecordRepository;
    private final CurrentStoreContextService currentStoreContextService;

    public AlertService(AlertRecordRepository alertRecordRepository, CurrentStoreContextService currentStoreContextService) {
        this.alertRecordRepository = alertRecordRepository;
        this.currentStoreContextService = currentStoreContextService;
    }

    public List<AlertResponse> getAlerts() {
        return alertRecordRepository.findAllByStoreIdOrderByCreatedAtDesc(currentStoreContextService.getCurrentStoreId()).stream()
            .map(this::toResponse)
            .toList();
    }

    @Transactional
    public AlertResponse acknowledgeAlert(UUID alertId) {
        AlertRecord alertRecord = alertRecordRepository.findByIdAndStoreId(alertId, currentStoreContextService.getCurrentStoreId())
            .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Alert not found"));
        alertRecord.setStatus(AlertStatus.ACKNOWLEDGED);
        alertRecord.setAcknowledgedAt(OffsetDateTime.now());
        return toResponse(alertRecordRepository.save(alertRecord));
    }

    public List<AlertResponse> getOpenAlerts() {
        return alertRecordRepository.findAllByStoreIdOrderByCreatedAtDesc(currentStoreContextService.getCurrentStoreId()).stream()
            .filter(alert -> alert.getStatus() == AlertStatus.OPEN)
            .map(this::toResponse)
            .toList();
    }

    private AlertResponse toResponse(AlertRecord alert) {
        return new AlertResponse(
            alert.getId(),
            alert.getStore().getId(),
            alert.getType(),
            alert.getSeverity(),
            alert.getStatus(),
            alert.getBusinessDate(),
            alert.getMessage(),
            alert.getCreatedAt(),
            alert.getAcknowledgedAt()
        );
    }
}
