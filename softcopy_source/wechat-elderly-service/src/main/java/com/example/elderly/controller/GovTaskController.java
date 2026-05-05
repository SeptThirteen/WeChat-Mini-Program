package com.example.elderly.controller;

import com.example.elderly.common.ApiResponse;
import com.example.elderly.dto.GovTaskRequest;
import com.example.elderly.entity.GovTask;
import com.example.elderly.service.GovTaskService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class GovTaskController {

    private final GovTaskService govTaskService;

    @PostMapping("/api/gov/submit")
    public ApiResponse<GovTask> submit(@Valid @RequestBody GovTaskRequest request) {
        return ApiResponse.success(govTaskService.submit(request));
    }

    @GetMapping("/api/gov/list")
    public ApiResponse<List<GovTask>> list(@RequestParam("userId") Long userId) {
        return ApiResponse.success(govTaskService.listByUser(userId));
    }
}
