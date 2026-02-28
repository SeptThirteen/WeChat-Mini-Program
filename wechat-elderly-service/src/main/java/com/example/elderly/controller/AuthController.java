package com.example.elderly.controller;

import com.example.elderly.common.ApiResponse;
import com.example.elderly.dto.LoginRequest;
import com.example.elderly.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/api/auth/login")
    public ApiResponse<Map<String, Object>> login(@Valid @RequestBody LoginRequest request) {
        return ApiResponse.success(authService.login(request));
    }

    @PostMapping("/api/auth/logout")
    public ApiResponse<Void> logout() {
        return ApiResponse.success(null);
    }
}
