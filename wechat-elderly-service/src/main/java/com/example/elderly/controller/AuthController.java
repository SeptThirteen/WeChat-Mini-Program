package com.example.elderly.controller;

import com.example.elderly.common.ApiResponse;
import com.example.elderly.dto.LoginRequest;
import com.example.elderly.dto.SmsCodeRequest;
import com.example.elderly.service.AuthService;
import com.example.elderly.service.SmsCodeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.Map;

@RestController
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final SmsCodeService smsCodeService;

    /** 演示项目未接短信网关：为 true 时接口回显 devCode 便于联调，生产环境必须置为 false */
    @Value("${sms.code.return-in-response:true}")
    private boolean returnCodeInResponse;

    @PostMapping("/api/auth/login")
    public ApiResponse<Map<String, Object>> login(@Valid @RequestBody LoginRequest request) {
        return ApiResponse.success(authService.login(request));
    }

    /**
     * 获取短信验证码（公开接口）
     * 演示模式下响应携带 devCode 字段；真实短信场景该字段不存在，验证码仅下发到手机。
     */
    @PostMapping("/api/auth/sms-code")
    public ApiResponse<Map<String, Object>> smsCode(@Valid @RequestBody SmsCodeRequest request) {
        String code = smsCodeService.send(request.getPhone());
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("phone", request.getPhone());
        data.put("expireSeconds", 300);
        if (returnCodeInResponse) {
            data.put("devCode", code);
        }
        return ApiResponse.success(data);
    }

    @PostMapping("/api/auth/logout")
    public ApiResponse<Void> logout() {
        return ApiResponse.success(null);
    }
}
