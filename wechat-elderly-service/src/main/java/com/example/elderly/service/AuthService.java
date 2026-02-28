package com.example.elderly.service;

import com.example.elderly.dto.LoginRequest;

import java.util.Map;

public interface AuthService {
    Map<String, Object> login(LoginRequest request);
}
