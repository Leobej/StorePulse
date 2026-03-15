package org.projects.dto;

public record AuthRequest(
    String username,
    String password
) {
}
