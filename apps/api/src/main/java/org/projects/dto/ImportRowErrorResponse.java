package org.projects.dto;

public record ImportRowErrorResponse(
    int lineNumber,
    String errorMessage
) {
}
