package com.example.elderly.service.impl;

import com.example.elderly.dto.LoginRequest;
import com.example.elderly.entity.User;
import com.example.elderly.service.AuthService;
import com.example.elderly.service.UserService;
import com.example.elderly.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserService userService;
    private final JwtUtil jwtUtil;

    @Override
    public Map<String, Object> login(LoginRequest request) {
        User user = userService.getByPhone(request.getPhone());
        if (user == null) {
            user = userService.createUser(request.getPhone());
        }

        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", user.getUserId());
        claims.put("phone", user.getPhone());
        String token = jwtUtil.generateToken(claims);

        Map<String, Object> result = new HashMap<>();
        result.put("token", token);
        result.put("userId", user.getUserId());
        result.put("phone", user.getPhone());
        result.put("name", user.getName());
        return result;
    }
}
