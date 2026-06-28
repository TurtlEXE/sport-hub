package com.mvc.mock_project.controller;

import com.mvc.mock_project.dto.request.LoginRequest;
import com.mvc.mock_project.dto.response.AuthResponse;
import com.mvc.mock_project.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthApiController {

    private final AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        AuthResponse response = authService.login(request);
        return ResponseEntity.ok(response);
    }
    
    // Other endpoints can be added here if this app is consumed by a mobile app
}
