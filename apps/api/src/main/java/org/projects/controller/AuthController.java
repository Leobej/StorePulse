package org.projects.controller;

import org.projects.dto.AuthRequest;
import org.projects.dto.AuthResponse;
import org.projects.dto.CurrentSessionResponse;
import org.projects.dto.SwitchStoreRequest;
import org.projects.service.AuthService;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
public class AuthController {
    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping(path = "/login", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<AuthResponse> login(@RequestBody AuthRequest authRequest) {
        return ResponseEntity.ok(authService.login(authRequest));
    }

    @GetMapping(path = "/me", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<CurrentSessionResponse> currentSession() {
        return ResponseEntity.status(HttpStatus.OK).body(authService.currentSession());
    }

    @PostMapping(path = "/switch-store", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<CurrentSessionResponse> switchStore(@RequestBody SwitchStoreRequest switchStoreRequest) {
        return ResponseEntity.ok(authService.switchStore(switchStoreRequest.storeId()));
    }
}
