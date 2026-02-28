package com.example.elderly.controller;

import com.example.elderly.common.ApiResponse;
import com.example.elderly.dto.UpdateUserRequest;
import com.example.elderly.entity.User;
import com.example.elderly.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/api/user/profile")
    public ApiResponse<User> profile(@RequestParam("userId") Long userId) {
        return ApiResponse.success(userService.getById(userId));
    }

    @PutMapping("/api/user/update")
    public ApiResponse<Map<String, Object>> update(@Valid @RequestBody UpdateUserRequest request) {
        userService.updateProfile(request);
        return ApiResponse.success(Map.of("userId", request.getUserId()));
    }

    @GetMapping("/api/user/list")
    public ApiResponse<List<User>> list() {
        return ApiResponse.success(userService.listAll());
    }

    @DeleteMapping("/api/user/{id}")
    public ApiResponse<Void> delete(@PathVariable("id") Long id) {
        userService.deleteById(id);
        return ApiResponse.success(null);
    }
}
